package com.getstream.sdk.chat.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.getstream.sdk.chat.viewmodel.ChannelHeaderViewModel
import com.getstream.sdk.chat.viewmodel.MessageInputViewModel
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel

/**
 * A ViewModel factory for MessageListViewModel, ChannelHeaderViewModel and MessageInputViewModel
 *
 * @param cid: the channel id in the format messaging:123
 *
 * @see MessageListViewModel
 * @see ChannelHeaderViewModel
 * @see MessageInputViewModel
 */
public class ChannelViewModelFactory(private val cid: String) : ViewModelProvider.Factory {
    private val factories: Map<Class<*>, () -> ViewModel> = mapOf(
        ChannelHeaderViewModel::class.java to { ChannelHeaderViewModel(cid) },
        MessageInputViewModel::class.java to { MessageInputViewModel(cid) },
        MessageListViewModel::class.java to { MessageListViewModel(cid) },
    )

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val viewModel: ViewModel? = factories[modelClass]?.invoke()
            ?: throw IllegalArgumentException("ChannelViewModelFactory can only create instances of the following classes: ${factories.keys.joinToString { it.simpleName }}")

        @Suppress("UNCHECKED_CAST")
        return viewModel as T
    }
}
