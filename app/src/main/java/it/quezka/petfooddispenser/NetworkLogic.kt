package it.quezka.petfooddispenser

import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.util.concurrent.TimeUnit

data class DispenserState(
    @SerializedName("cr1") val cr1: Float = 1f,
    @SerializedName("cr2") val cr2: Float = 1f,
    @SerializedName("cr3") val cr3: Float = 1f,
    @SerializedName("cr1_r") val cr1Remote: Float = 1f,
    @SerializedName("cr2_r") val cr2Remote: Float = 1f,
    @SerializedName("cr3_r") val cr3Remote: Float = 1f,
    @SerializedName("mode") val mode: String = "local"
)

fun interface NetworkManagerFactory {
    operator fun invoke(serverIP: String): NetworkManager
}

class NetworkManager(private val serverIP: String) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS) // SSE needs 0 for infinite read timeout
        .build()

    private val sseFactory = EventSources.createFactory(client)

    suspend fun fetchStatus(): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url("http://$serverIP/get?t=${System.currentTimeMillis()}")
                .build()
            
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
                response.body?.string() ?: throw Exception("Empty body")
            }
        }
    }

    fun startSse(onMessage: (String) -> Unit, onError: (Throwable) -> Unit): EventSource {
        val request = Request.Builder()
            .url("http://$serverIP/events") // Your Arduino should serve events here
            .header("Accept", "text/event-stream")
            .build()

        val listener = object : EventSourceListener() {
            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                onMessage(data)
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                onError(t ?: Exception("Unknown SSE Error"))
            }
            
            override fun onClosed(eventSource: EventSource) {
                onError(Exception("SSE Connection Closed"))
            }
        }

        return sseFactory.newEventSource(request, listener)
    }

    suspend fun sendCommand(endpoint: String, variable: String, value: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url("http://$serverIP/$endpoint?$variable=$value")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
                response.body?.string() ?: ""
            }
        }
    }
}
