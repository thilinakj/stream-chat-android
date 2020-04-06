package io.getstream.chat.android.client.socket

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.models.User

interface ChatSocketService {

    var state: State

    fun connect(
        endpoint: String,
        apiKey: String,
        user: User?,
        userToken: String?
    )

    fun disconnect()

    fun addListener(listener: SocketListener)
    fun removeListener(listener: SocketListener)

    sealed class State {
        object Disconnected : State()
        object Connecting : State()
        class Connected(val event: ConnectedEvent) : State()
        class Error(val error: ChatError) : State()
    }
}