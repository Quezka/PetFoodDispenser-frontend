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
import kotlinx.coroutines.delay
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
    private var isRetryInProgress = false
    private var currentIp: String = ""

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
        createNotificationChannels()
        startForeground(NOTIFICATION_ID, createNotification("Connecting...", false))

        lifecycleScope.launch {
            settingsRepository.serverIp.collect { ip ->
                if (ip.isNotBlank()) {
                    currentIp = ip
                    networkManager = networkManagerFactory(ip)
                    connectSse()
                } else {
                    currentIp = ""
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
                isRetryInProgress = false
                try {
                    val state = gson.fromJson(json, DispenserState::class.java)
                    stateManager.updateState(state)
                    stateManager.setConnected(true)
                    updateNotification("Connected to Dispenser", false)
                } catch (e: Exception) {
                    // Log error
                }
            },
            onError = {
                stateManager.setConnected(false)
                updateNotification("Connection Lost", false)
                scheduleReconnect()
            }
        )
    }

    private fun scheduleReconnect() {
        if (isRetryInProgress || currentIp.isBlank()) return
        isRetryInProgress = true
        lifecycleScope.launch {
            var retryDelay = 2000L
            while (isRetryInProgress && currentIp.isNotBlank()) {
                delay(retryDelay)
                if (!stateManager.isConnected.value) {
                    connectSse()
                } else {
                    isRetryInProgress = false
                }
                retryDelay = (retryDelay * 2).coerceAtMost(60000L) // Exponential backoff up to 1 min
            }
        }
    }

    private fun disconnectSse() {
        sseConnection?.cancel()
        sseConnection = null
        stateManager.setConnected(false)
        isRetryInProgress = false
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Channel for normal status
            val statusChannel = NotificationChannel(
                CHANNEL_ID_STATUS,
                "Dispenser Status",
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(statusChannel)

            // Channel for alerts
            val alertChannel = NotificationChannel(
                CHANNEL_ID_ALERTS,
                "Dispenser Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableLights(true)
                enableVibration(true)
            }
            manager.createNotificationChannel(alertChannel)
        }
    }

    private fun createNotification(content: String, isAlert: Boolean): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = if (isAlert) CHANNEL_ID_ALERTS else CHANNEL_ID_STATUS

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Pet Food Dispenser")
            .setContentText(content)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(!isAlert)
            .setPriority(if (isAlert) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(isAlert)
            .build()
    }

    private fun updateNotification(content: String, isAlert: Boolean) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (isAlert) {
            notificationManager.notify(NOTIFICATION_ID_ALERT, createNotification(content, true))
        } else {
            notificationManager.notify(NOTIFICATION_ID, createNotification(content, false))
        }
    }

    override fun onDestroy() {
        disconnectSse()
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID_STATUS = "dispenser_channel_status"
        private const val CHANNEL_ID_ALERTS = "dispenser_channel_alerts"
        private const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_ID_ALERT = 2
    }
}
