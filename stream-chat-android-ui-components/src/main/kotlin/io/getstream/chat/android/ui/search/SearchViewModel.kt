package io.getstream.chat.android.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.image
import io.getstream.chat.android.client.models.name
import java.util.Date

public class SearchViewModel : ViewModel() {

    private val _results: MutableLiveData<List<Message>> = MutableLiveData()
    public val results: LiveData<List<Message>> = _results

    init {
        fetchDummyResults()
    }

    private fun fetchDummyResults() {
        _results.value = List(20) {
            Message(
                id = "dummy-id-$it",
                user = User().apply {
                    name = "John Doe"
                    image = "https://randomuser.me/api/portraits/men/0.jpg"
                },
                createdAt = Date(2020, 3, 15, 18, 11),
                text = "Is it me you're looking for?",
            )
        }
    }

    public fun setQuery(query: String) {
        fetchDummyResults()
    }
}
