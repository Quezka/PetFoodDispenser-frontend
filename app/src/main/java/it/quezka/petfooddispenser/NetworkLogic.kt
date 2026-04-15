package it.quezka.petfooddispenser

import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
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
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(3, TimeUnit.SECONDS)
        .build()

    suspend fun fetchStatus(): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url("http://$serverIP/get?t=${System.currentTimeMillis()}")
                .header("Cache-Control", "no-cache")
                .build()
            
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
                val body = response.body?.string() ?: throw Exception("Empty body")
                body
            }
        }
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
