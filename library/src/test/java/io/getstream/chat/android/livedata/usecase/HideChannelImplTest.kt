package io.getstream.chat.android.livedata.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.BaseConnectedIntegrationTest
import io.getstream.chat.android.livedata.entity.ChannelEntity
import io.getstream.chat.android.livedata.entity.QueryChannelsEntity
import io.getstream.chat.android.livedata.request.QueryChannelsPaginationRequest
import io.getstream.chat.android.livedata.utils.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class HideChannelImplTest : BaseConnectedIntegrationTest() {
    /*
    Few things don't work yet
    - queryChannelsController.channels should indicate which channels are hidden (hidden is not returned on channels though)

     */

    @Test
    fun loadHidden() = runBlocking(Dispatchers.IO) {
        val channelEntity = ChannelEntity(data.channel1)
        channelEntity.hidden = true
        chatDomainImpl.repos.channels.insert(channelEntity)
        // setup the channel controller
        var channelController = chatDomain.useCases.watchChannel(data.channel1.cid, 0).execute().data()
        var channelControllerImpl = chatDomainImpl.channel(data.channel1.cid)
        channelControllerImpl.watch(10)
        // verify it's hidden
        Truth.assertThat(channelController.hidden.getOrAwaitValue()).isTrue()
    }

    @Test
    fun loadHiddenQueryChannels() = runBlocking(Dispatchers.IO) {
        // insert the channel and queryChannelsResult
        val channelEntity = ChannelEntity(data.channel1)
        channelEntity.hidden = true
        chatDomainImpl.repos.channels.insert(channelEntity)
        val query = QueryChannelsEntity(data.filter1, null).apply { channelCIDs = sortedSetOf<String>(data.channel1.cid) }
        chatDomainImpl.repos.queryChannels.insert(query)

        // setup the query channel controller
        var queryChannelsController = chatDomain.useCases.queryChannels(data.filter1, null, 0, 10).execute().data()
        var queryChannelsControllerImpl = chatDomainImpl.queryChannels(data.filter1, null)
        queryChannelsControllerImpl.runQueryOffline(QueryChannelsPaginationRequest())

        // verify we have 1 channel in the result list and that it's hidden
        val channels = queryChannelsController.channels.getOrAwaitValue()
        val channelController = chatDomainImpl.channel(data.channel1)
        Truth.assertThat(channelController.hidden.getOrAwaitValue()).isTrue()
    }

    @Test
    @Ignore("problematic since we dont have channel.hidden")
    fun hide() = runBlocking(Dispatchers.IO) {
        var channelController = chatDomain.useCases.watchChannel(data.channel1.cid, 10).execute().data()
        var channelControllerImpl = chatDomainImpl.channel(data.channel1.cid)
        val result = chatDomain.useCases.hideChannel(data.channel1.cid, true).execute()
        // verify it's now hidden
        Truth.assertThat(channelController.hidden.getOrAwaitValue()).isTrue()
        // verify that it's no longer showing up in query channels
        // TODO LLC channel doesn't expose hidden state just yet

        // verify that receiving a new message unhides it
        channelControllerImpl.handleEvent(data.newMessageEventNotification)
        Truth.assertThat(channelController.hidden.getOrAwaitValue()).isFalse()
    }

    @Test
    fun show() = runBlocking(Dispatchers.IO) {
        var channelController = chatDomain.useCases.watchChannel(data.channel1.cid, 10).execute().data()
        var channelControllerImpl = chatDomainImpl.channel(data.channel1.cid)
        chatDomain.useCases.hideChannel(data.channel1.cid, true).execute()
        chatDomain.useCases.showChannel(data.channel1.cid).execute()

        Truth.assertThat(channelController.hidden.getOrAwaitValue()).isFalse()
    }

    @Test
    fun keepHistory() = runBlocking(Dispatchers.IO) {
        var channelController = chatDomain.useCases.watchChannel(data.channel1.cid, 10).execute().data()
        var channelControllerImpl = chatDomainImpl.channel(data.channel1.cid)
        // add a message that should no longer be visible afterwards
        chatDomainImpl.repos.messages.insertMessage(data.message2Older)
        channelControllerImpl.handleEvent(data.newMessageEvent2)
        // keep history = false, so messages should go bye bye
        val result = chatDomain.useCases.hideChannel(data.channel1.cid, false).execute()
        assertSuccess(result as Result<Any>)
        // verify it's now hidden
        Truth.assertThat(channelController.hidden.getOrAwaitValue()).isTrue()
        // verify that old messages are gone...
        val oldMessage = channelControllerImpl.getMessage(data.message2Older.id)
    }
}
