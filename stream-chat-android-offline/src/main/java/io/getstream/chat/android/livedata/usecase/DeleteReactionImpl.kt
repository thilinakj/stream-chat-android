package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.utils.validateCid

public interface DeleteReaction {
    /**
     * Deletes the specified reaction, request is retried according to the retry policy specified on the chatDomain
     * @param cid the full channel id, ie messaging:123
     * @param reaction the reaction to mark as deleted
     * @return A call object with Message as the return type
     * @see io.getstream.chat.android.livedata.utils.RetryPolicy
     */
    public operator fun invoke(cid: String, reaction: Reaction): Call<Message>
}

internal class DeleteReactionImpl(private val domainImpl: ChatDomainImpl) : DeleteReaction {
    override operator fun invoke(cid: String, reaction: Reaction): Call<Message> {
        validateCid(cid)

        val channelController = domainImpl.channel(cid)
        return CoroutineCall(domainImpl.scope) {
            channelController.deleteReaction(reaction)
        }
    }
}
