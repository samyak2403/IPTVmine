package com.samyak2403.iptvmine.provider

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.samyak2403.iptvmine.model.Channel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL



class ChannelsProvider : ViewModel() {

    private val _channels = MutableLiveData<List<Channel>>()
    val channels: LiveData<List<Channel>> = _channels

    private val _filteredChannels = MutableLiveData<List<Channel>>()
    val filteredChannels: LiveData<List<Channel>> = _filteredChannels

    private val sourceUrl = "https://raw.githubusercontent.com/aniketda/iptv2050/main/iptv"

    // Fetch the M3U file from the provided URL
    fun fetchM3UFile() {
        CoroutineScope(Dispatchers.IO).launch {
            val url = URL(sourceUrl)
            val urlConnection = url.openConnection() as HttpURLConnection
            try {
                val fileText = urlConnection.inputStream.bufferedReader().readText()
                val lines = fileText.split("\n")

                val tempChannels = mutableListOf<Channel>()

                var name: String? = null
                var logoUrl: String? = null
                var streamUrl: String? = null

                for (line in lines) {
                    when {
                        line.startsWith("#EXTINF:") -> {
                            val parts = line.split(",")
                            name = parts.getOrNull(1)
                            val logoParts = parts[0].split("\"")
                            logoUrl = if (logoParts.size > 3) {
                                logoParts[3]
                            } else {
                                "https://fastly.picsum.photos/id/125/536/354.jpg?hmac=EYT3s6VXrAoggrr4fXsOIIcQ3Grc13fCmXkqcE2FusY"
                            }
                        }
                        line.isNotEmpty() -> {
                            streamUrl = line
                            if (!name.isNullOrEmpty()) {
                                tempChannels.add(
                                    Channel(
                                        name = name,
                                        logoUrl = logoUrl ?: "https://fastly.picsum.photos/id/928/200/200.jpg?hmac=5MQxbf-ANcu87ZaOn5sOEObpZ9PpJfrOImdC7yOkBlg",
                                        streamUrl = streamUrl
                                    )
                                )
                            }
                            // Reset variables for the next channel
                            name = null
                            logoUrl = null
                            streamUrl = null
                        }
                    }
                }

                // Update LiveData on the main thread
                withContext(Dispatchers.Main) {
                    _channels.value = tempChannels
                }
            } finally {
                urlConnection.disconnect()
            }
        }
    }

    // Filter channels based on the search query
    fun filterChannels(query: String) {
        val filtered = _channels.value?.filter {
            it.name.contains(query, ignoreCase = true)
        } ?: emptyList()
        _filteredChannels.value = filtered
    }
}
