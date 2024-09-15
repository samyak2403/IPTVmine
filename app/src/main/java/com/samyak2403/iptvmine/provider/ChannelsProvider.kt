package com.samyak2403.iptvmine.provider

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samyak2403.iptvmine.model.Channel
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL

class ChannelsProvider : ViewModel() {

    private val _channels = MutableLiveData<List<Channel>>()
    val channels: LiveData<List<Channel>> = _channels

    private val _filteredChannels = MutableLiveData<List<Channel>>()
    val filteredChannels: LiveData<List<Channel>> = _filteredChannels

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val sourceUrl = "https://raw.githubusercontent.com/FunctionError/PiratesTv/main/combined_playlist.m3u"

    private var fetchJob: Job? = null

    // Fetch the M3U file from the provided URL
    fun fetchM3UFile() {
        fetchJob?.cancel() // Cancel any ongoing fetch

        fetchJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val urlConnection = (URL(sourceUrl).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 10000 // 10 seconds timeout
                    readTimeout = 10000
                }

                urlConnection.inputStream.bufferedReader().use { reader ->
                    val fileText = reader.readText()
                    val tempChannels = parseM3UFile(fileText)

                    // Update LiveData on the main thread
                    withContext(Dispatchers.Main) {
                        _channels.value = tempChannels
                        _error.value = null
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.value = "Failed to fetch channels: ${e.message}"
                }
            }
        }
    }

    // Parse M3U file content and return a list of Channel objects
    private fun parseM3UFile(fileText: String): List<Channel> {
        val lines = fileText.split("\n")
        val tempChannels = mutableListOf<Channel>()

        var name: String? = null
        var logoUrl: String = getDefaultLogoUrl()
        var streamUrl: String? = null

        for (line in lines) {
            when {
                line.startsWith("#EXTINF:") -> {
                    name = extractChannelName(line)
                    logoUrl = extractLogoUrl(line) ?: getDefaultLogoUrl()
                }
                line.isNotEmpty() -> {
                    streamUrl = line
                    if (!name.isNullOrEmpty() && streamUrl != null) {
                        tempChannels.add(
                            Channel(
                                name = name,
                                logoUrl = logoUrl,
                                streamUrl = streamUrl
                            )
                        )
                    }
                    // Reset variables for the next channel
                    name = null
                    logoUrl = getDefaultLogoUrl()
                    streamUrl = null
                }
            }
        }
        return tempChannels
    }

    private fun getDefaultLogoUrl(): String {
        return "assets/images/ic_tv.png"
    }

    private fun extractChannelName(line: String): String? {
        val parts = line.split(",")
        return parts.lastOrNull()?.trim()
    }

    private fun extractLogoUrl(line: String): String? {
        val parts = line.split("\"")
        return when {
            parts.size > 1 && isValidUrl(parts[1]) -> parts[1]
            parts.size > 5 && isValidUrl(parts[5]) -> parts[5]
            else -> null
        }
    }

    private fun isValidUrl(url: String): Boolean {
        return url.startsWith("https") || url.startsWith("http")
    }

    // Filter channels based on the user query
    fun filterChannels(query: String) {
        _channels.value?.let { channelList ->
            val filtered = channelList.filter { it.name.contains(query, ignoreCase = true) }
            _filteredChannels.value = filtered
        }
    }

    // Cancel any ongoing fetch when ViewModel is cleared
    override fun onCleared() {
        super.onCleared()
        fetchJob?.cancel()
    }
}


//
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import com.samyak2403.iptvmine.model.Channel
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import java.net.HttpURLConnection
//import java.net.URL
//
//
//
//class ChannelsProvider : ViewModel() {
//
//    private val _channels = MutableLiveData<List<Channel>>()
//    val channels: LiveData<List<Channel>> = _channels
//
//    private val _filteredChannels = MutableLiveData<List<Channel>>()
//    val filteredChannels: LiveData<List<Channel>> = _filteredChannels
//
////    private val sourceUrl = "https://raw.githubusercontent.com/aniketda/iptv2050/main/iptv"
//    private val sourceUrl = "https://raw.githubusercontent.com/FunctionError/PiratesTv/main/combined_playlist.m3u'"
//
//    // Fetch the M3U file from the provided URL
//    fun fetchM3UFile() {
//        CoroutineScope(Dispatchers.IO).launch {
//            val url = URL(sourceUrl)
//            val urlConnection = url.openConnection() as HttpURLConnection
//            try {
//                val fileText = urlConnection.inputStream.bufferedReader().readText()
//                val lines = fileText.split("\n")
//
//                val tempChannels = mutableListOf<Channel>()
//
//                var name: String? = null
//                var logoUrl: String? = null
//                var streamUrl: String? = null
//
//                for (line in lines) {
//                    when {
//                        line.startsWith("#EXTINF:") -> {
//                            val parts = line.split(",")
//                            name = parts.getOrNull(1)
//                            val logoParts = parts[0].split("\"")
//                            logoUrl = if (logoParts.size > 3) {
//                                logoParts[3]
//                            } else {
//                                "https://fastly.picsum.photos/id/125/536/354.jpg?hmac=EYT3s6VXrAoggrr4fXsOIIcQ3Grc13fCmXkqcE2FusY"
//                            }
//                        }
//                        line.isNotEmpty() -> {
//                            streamUrl = line
//                            if (!name.isNullOrEmpty()) {
//                                tempChannels.add(
//                                    Channel(
//                                        name = name,
//                                        logoUrl = logoUrl ?: "https://fastly.picsum.photos/id/928/200/200.jpg?hmac=5MQxbf-ANcu87ZaOn5sOEObpZ9PpJfrOImdC7yOkBlg",
//                                        streamUrl = streamUrl
//                                    )
//                                )
//                            }
//                            // Reset variables for the next channel
//                            name = null
//                            logoUrl = null
//                            streamUrl = null
//                        }
//                    }
//                }
//
//                // Update LiveData on the main thread
//                withContext(Dispatchers.Main) {
//                    _channels.value = tempChannels
//                }
//            } finally {
//                urlConnection.disconnect()
//            }
//        }
//    }
//
//    // Filter channels based on the search query
//    fun filterChannels(query: String) {
//        val filtered = _channels.value?.filter {
//            it.name.contains(query, ignoreCase = true)
//        } ?: emptyList()
//        _filteredChannels.value = filtered
//    }
//}
