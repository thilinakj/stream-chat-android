package io.getstream.chat.android.client.events

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User

class NewMessageEvent : RemoteEvent() {
    lateinit var cid: String
    lateinit var message: Message
    lateinit var user: User
    @SerializedName("watcher_count")
    val watcherCount: Int = 0
}