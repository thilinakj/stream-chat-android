package com.getstream.sdk.chat.adapter

import android.util.Log
import androidx.recyclerview.widget.DiffUtil

internal object MessageListItemDiffCallback : DiffUtil.ItemCallback<MessageListItem>() {
    override fun areItemsTheSame(oldItem: MessageListItem, newItem: MessageListItem): Boolean {
        return oldItem.getStableId() == newItem.getStableId()
    }

    /**
     * There are a few scenarios which shouldn't trigger a message list rerender:
     *
     * - Message.user can have
     * -- different unread counts, we shouldn't rerender when that happens
     * -- different last online values, again shouldn't trigger a rerender
     * - The user objects on message.latestReactions and message.ownReactions have the same issue with last online and unread counts values on the user object
     * - The message.html value can be different which we don't use in our SDK
     * - messageReadBy returns a list of channel user read objects, the problem is that they contain unread message counts which can trigger a diff
     * - reaction.updated at wasn't stored in the reaction entity, always triggering a rerender
     */
    override fun areContentsTheSame(oldItem: MessageListItem, newItem: MessageListItem): Boolean {

        val isTheSame = oldItem == newItem

        if (!isTheSame) {
            if (oldItem is MessageListItem.MessageItem && newItem is MessageListItem.MessageItem) {

                if (oldItem.message.user != newItem.message.user) {
                    Log.i("areContentsTheSameU", "User: oldItem doesn't equal new item \n${oldItem.message.user}\n${newItem.message.user}")
                } else if (oldItem.message != newItem.message) {
                    Log.i("areContentsTheSameM", "areContentsTheSame: oldItem doesn't equal new item \n${oldItem.message}\n${newItem.message}")
                } else {
                    Log.i("areContentsTheSameI", "The items aren't equal")
                }
            } else {
                Log.i("areContentsTheSameOther", "areContentsTheSame: oldItem doesn't equal new item \n$oldItem\n$newItem")
            }
        }
        return isTheSame
    }
}
