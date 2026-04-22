package it.quezka.petfooddispenser

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import okhttp3.sse.EventSource
import javax.inject.Inject

@AndroidEntryPoint
class DispenserService : LifecycleService() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var networkManagerFactory: NetworkManagerFactory

    @Inject
    lateinit var stateManager: DispenserStateManager

    private var networkManager: NetworkManager? = null
    private var sseConnection: EventSource? = null
    private val gson = com.google.gson.Gson()

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): DispenserService = this@DispenserService
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification("Connecting..."))

        lifecycleScope.launch {
            settingsRepository.serverIp.collect { ip ->
                if (ip.isNotBlank()) {
                    networkManager = networkManagerFactory(ip)
                    connectSse()
                } else {
                    disconnectSse()
                }
            }
        }
    }

    private fun connectSse() {
        disconnectSse()
        val manager = networkManager ?: return

        sseConnection = manager.startSse(
            onMessage = { json ->
                try {
                    val state = gson.fromJson(json, DispenserState::class.java)
                    stateManager.updateState(state)
                    stateManager.setConnected(true)
                    updateNotification("Connected to Dispenser")
                } catch (e: Exception) {
                    // Log error
                }
            },
            onError = {
                stateManager.setConnected(false)
                updateNotification("Connection Lost")
            }
        )
    }

    private fun disconnectSse() {
        sseConnection?.cancel()
        sseConnection = null
        stateManager.setConnected(false)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Dispenser Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(content: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Pet Food Dispenser")
            .setContentText(content)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(content: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification(content))
    }

    override fun onDestroy() {
        disconnectSse()
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID = "dispenser_channel"
        private const val NOTIFICATION_ID = 1
    }
}
