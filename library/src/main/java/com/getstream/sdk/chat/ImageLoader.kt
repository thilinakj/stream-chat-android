package com.getstream.sdk.chat

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import coil.Coil
import coil.request.GetRequest
import coil.request.GetRequestBuilder
import coil.transform.BlurTransformation
import coil.transform.CircleCropTransformation
import coil.transform.GrayscaleTransformation
import coil.transform.RoundedCornersTransformation
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal object ImageLoader {
	var numCalls = 0
	var totalTime = 0.0
	suspend fun getBitmap(context: Context,
	                      url: String,
	                      transformation: ImageTransformation = ImageTransformation.None
	): Bitmap? = withContext(Dispatchers.IO) {
		url.takeUnless { it.isBlank() }
				?.let {
					val startupTime = System.nanoTime()
					getBitmapWithCoil(context, url, transformation).
//					getBitmapWithGlide(context, url, transformation).
					also { addNewCallTime(System.nanoTime() - startupTime) }
				}
	}

	private suspend fun getBitmapWithGlide(context: Context,
	                                       url: String,
	                                       transformation: ImageTransformation) =
			Glide.with(context)
					.asBitmap()
					.load(url)
					.apply(RequestOptions.circleCropTransform())
					.submit()
					.get()

	private suspend fun getBitmapWithCoil(context: Context,
	                                      url: String,
	                                      transformation: ImageTransformation) =
			(Coil.execute(GetRequest.Builder(context)
					.data(url)
					.applyTransformation(transformation, context)
					.build())
					.drawable as? BitmapDrawable)?.bitmap

	private fun addNewCallTime(timeSpend: Long) {
		synchronized(this) {
			numCalls ++
			totalTime += timeSpend
			println("Benchmark: Call #$numCalls -> AverageTime: ${totalTime / numCalls}")
		}
	}

	private fun GetRequestBuilder.applyTransformation(transformation: ImageTransformation, context: Context): GetRequestBuilder =
			when (transformation) {
				is ImageTransformation.None -> this
				is ImageTransformation.Circle -> transformations(CircleCropTransformation())
				is ImageTransformation.Grayscale -> transformations(GrayscaleTransformation())
				is ImageTransformation.Blur -> transformations(BlurTransformation(context, transformation.radius, transformation.sampling))
				is ImageTransformation.RoundedCorners -> transformations(RoundedCornersTransformation(transformation.radius))
			}

	sealed class ImageTransformation {
		object None : ImageTransformation()
		object Circle : ImageTransformation()
		object Grayscale : ImageTransformation()
		class Blur(val radius: Float = 10f, val sampling: Float = 1f) : ImageTransformation()
		class RoundedCorners(val radius: Float) : ImageTransformation()
	}
}