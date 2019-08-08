package com.getstream.sdk.chat.rest.response;

import android.text.TextUtils;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.model.Watcher;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.Member;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.utils.Global;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class ChannelState {

    private final String TAG = ChannelState.class.getSimpleName();

    @SerializedName("channel")
    private Channel channel;

    @SerializedName("messages")
    private List<Message> messages;

    @SerializedName("read")
    private List<ChannelUserRead> reads;

    @SerializedName("members")
    private List<Member> members;

    @SerializedName("watchers")
    private List<Watcher> watchers;

    @SerializedName("watcher_count")
    private int watcherCount;

    public ChannelState() {
        channel = null;
        messages = new ArrayList<>();
        reads = new ArrayList<>();
        members = new ArrayList<>();
        Log.i(TAG, "ChannelState constructor... ");
    }

    // endregion
    public static void sortUserReads(List<ChannelUserRead> reads) {
        Collections.sort(reads, (ChannelUserRead o1, ChannelUserRead o2) -> o1.getLast_read().compareTo(o2.getLast_read()));
    }

    public Channel getChannel() {
        return channel;
    }

    public Message getOldestMessage() {
        if (messages == null || messages.size() == 0) {
            return null;
        }
        return messages.get(messages.size() - 1);
    }

    public List<Message> getMessages() {
        return messages;
    }

    public List<ChannelUserRead> getReads() {
        return reads;
    }

    public List<Member> getMembers() {
        return members;
    }

    public Message getLastMessage() {
        Message lastMessage = null;
        try {
            List<Message> messages = getMessages();
            for (int i = messages.size() - 1; i >= 0; i--) {
                Message message = messages.get(i);
                if (TextUtils.isEmpty(message.getDeletedAt()) && message.getType().equals(ModelType.message_regular)) {
                    lastMessage = message;
                    break;
                }
            }
            Message.setStartDay(Arrays.asList(lastMessage), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lastMessage;
    }

    public Message getLastMessageFromOtherUser() {
        Message lastMessage = null;
        try {
            List<Message> messages = getMessages();
            for (int i = messages.size() - 1; i >= 0; i--) {
                Message message = messages.get(i);
                if (TextUtils.isEmpty(message.getDeletedAt()) && !message.getUser().isMe()) {
                    lastMessage = message;
                    break;
                }
            }
            Message.setStartDay(Arrays.asList(lastMessage), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lastMessage;
    }

    public User getLastReader() {
        return null;
//        if (this.reads == null || this.reads.isEmpty()) return null;
//        User lastReadUser = null;
//        try {
//            if (!isSorted && this.reads != null) {
//                Global.sortUserReads(this.reads);
//                isSorted = true;
//            }
//            for (int i = reads.size() - 1; i >= 0; i--) {
//                ChannelUserRead channelUserRead = reads.get(i);
//                if (!channelUserRead.getUser().getId().equals(Global.client.user.getId())) {
//                    lastReadUser = channelUserRead.getUser();
//                    break;
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return lastReadUser;
    }

    public static Comparator<Message> byDate = (Message a, Message b) -> a.getCreatedAt().compareTo(b.getCreatedAt());

    public void addOrUpdateMessage(Message newMessage){
        for (int i = messages.size() - 1; i >= 0; i--) {
            if (messages.get(i).getId().equals(newMessage.getId())) {
                messages.set(i, newMessage);
                return;
            }
        }
        messages.add(newMessage);
        return;
    }

    public void addMessagesSorted(List<Message> messages){
        int initialSize = messages.size();
        for (Message m : messages) {
            if(m.getParentId() == null) {
                addOrUpdateMessage(m);
            }
        }
        if (initialSize != messages.size()) {
            Collections.sort(messages, byDate);
        }
    }

    public void init(ChannelState incoming) {
        //TODO: do an actual init instead of a replacement
        reads = incoming.reads;
        members = incoming.members;

        if (incoming.members != null && incoming.members.size() > 0) {
            for (Member m : incoming.members) {
                //TODO: update user references to client
            }
        }

        addMessagesSorted(incoming.messages);
        watcherCount = incoming.watcherCount;

        watchers = incoming.watchers;
        if (incoming.watchers != null && incoming.watchers.size() != 0) {
            // TODO: init watchers
        }

        reads = incoming.reads;
        if (incoming.reads != null && incoming.reads.size() != 0) {
            // TODO: init read state
        }

        members = incoming.members;
        if (incoming.members != null && incoming.members.size() != 0) {
            // TODO: init read state
        }

    }

    public int getUnreadMessageCount(String userId) {
        int unreadMessageCount = 0;
        if (this.reads == null || this.reads.isEmpty()) return unreadMessageCount;

        Date lastReadDate = getReadDateOfChannelLastMessage(userId);
        if (lastReadDate == null) return unreadMessageCount;
        for (int i = messages.size() - 1; i >= 0; i--) {
            Message message = messages.get(i);
            if (!message.isIncoming()) continue;
            if (!TextUtils.isEmpty(message.getDeletedAt())) continue;
//            if (!Global.readMessage(lastReadDate, message.getCreatedAt()))
//                unreadMessageCount++;
        }
        return unreadMessageCount;
    }

    public Date getReadDateOfChannelLastMessage(String userId) {
        if (this.reads == null || this.reads.isEmpty()) return null;
        Date lastReadDate = null;
        sortUserReads(this.reads);

        try {
            for (int i = reads.size() - 1; i >= 0; i--) {
                ChannelUserRead channelUserRead = reads.get(i);
                if (channelUserRead.getUser().getId().equals(userId)) {
                    lastReadDate = channelUserRead.getLast_read();
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return lastReadDate;
    }

    public void setReadDateOfChannelLastMessage(User user, Date readDate) {
        if (this.reads == null || this.reads.isEmpty()) return;
        boolean isNotSet = true;
        for (ChannelUserRead userLastRead : this.reads) {
            try {
                User user_ = userLastRead.getUser();
                if (user_.getId().equals(user.getId())) {
                    userLastRead.setLast_read(readDate);
                    // Change Order
                    this.reads.remove(userLastRead);
                    this.reads.add(userLastRead);
                    isNotSet = false;
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (isNotSet) {
            ChannelUserRead channelUserRead = new ChannelUserRead();
            channelUserRead.setUser(user);
            channelUserRead.setLast_read(readDate);
            this.reads.add(channelUserRead);
        }
    }
}

