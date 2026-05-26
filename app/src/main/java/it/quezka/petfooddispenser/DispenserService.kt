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
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.google.gson.JsonParser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import okhttp3.sse.EventSource
import javax.inject.Inject

@AndroidEntryPoint
class DispenserService : LifecycleService() {

    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var networkManagerFactory: NetworkManagerFactory
    @Inject lateinit var stateManager: DispenserStateManager

    private var networkManager: NetworkManager? = null
    private var sseConnection: EventSource? = null

    private var currentIp: String = ""
    private var isSseConnected = false
    private var reconnectJob: Job? = null

    inner class LocalBinder : Binder() { fun getService() = this@DispenserService }
    override fun onBind(intent: Intent): IBinder { super.onBind(intent); return LocalBinder() }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        startForeground(
            NOTIFICATION_ID,
            createNotification(getString(R.string.notification_connecting), false)
        )

        lifecycleScope.launch {
            settingsRepository.serverIp
                .distinctUntilChanged()
                .collect { ip ->
                    val trimmed = ip.trim()
                    currentIp = trimmed

                    if (trimmed.isBlank()) {
                        stopSse()
                        return@collect
                    }

                    networkManager = networkManagerFactory(trimmed)
                    restartSse()
                }
        }
    }

    // -----------------------------
    // SSE CONTROL
    // -----------------------------

    private fun restartSse() {
        stopSse()
        startSse()
    }

    private fun startSse() {
        val manager = networkManager ?: return

        sseConnection = manager.startSse(
            onOpen = {
                Log.d("DispenserService", "SSE onOpen")
                isSseConnected = true
                stateManager.setConnected(true)
                updateNotification(getString(R.string.notification_connected), false)
                stopReconnectLoop()
            },
            onEvent = { eventName, data ->
                Log.d("DispenserService", "SSE event: $eventName, data: $data")

                if (!isSseConnected) {
                    isSseConnected = true
                    stateManager.setConnected(true)
                    stopReconnectLoop()
                }

                handleSseEvent(eventName, data)
            },
            onError = { t ->
                Log.e("DispenserService", "SSE onError", t)
                isSseConnected = false
                stateManager.setConnected(false)
                updateNotification(getString(R.string.notification_lost), false)
                startReconnectLoop()
            }
        )
    }

    private fun stopSse() {
        Log.d("DispenserService", "Stopping SSE")
        isSseConnected = false
        sseConnection?.cancel()
        sseConnection = null
        stateManager.setConnected(false)
        stopReconnectLoop()
    }

    // -----------------------------
    // RECONNECT LOOP
    // -----------------------------

    private fun startReconnectLoop() {
        if (reconnectJob?.isActive == true) return
        if (currentIp.isBlank()) return

        reconnectJob = lifecycleScope.launch {
            var delayMs = 2000L

            while (!isSseConnected && currentIp.isNotBlank()) {
                Log.d("DispenserService", "Reconnect in ${delayMs}ms...")
                delay(delayMs)

                if (isSseConnected || currentIp.isBlank()) break

                Log.d("DispenserService", "Trying SSE reconnect...")
                startSse()

                delayMs = (delayMs * 2).coerceAtMost(60000L)
            }
        }
    }

    private fun stopReconnectLoop() {
        reconnectJob?.cancel()
        reconnectJob = null
    }

    // -----------------------------
    // SSE EVENT HANDLING
    // -----------------------------

    private fun handleSseEvent(eventName: String?, data: String) {
        val json = JsonParser.parseString(data).asJsonObject

        when (eventName) {

            // knobUpdate: {"cr1":..,"cr2":..,"cr3":..}
            "knobUpdate" -> {
                val cr1 = json["cr1"].asFloat
                val cr2 = json["cr2"].asFloat
                val cr3 = json["cr3"].asFloat

                val current = stateManager.state.value
                stateManager.updateState(
                    current.copy(
                        cr1 = cr1,
                        cr2 = cr2,
                        cr3 = cr3
                    )
                )
            }

            // erogation: {"is_erogating":0/1}
            "erogation" -> {
                val isErogating = json["is_erogating"].asInt == 1
                stateManager.setErogating(isErogating)
            }

            // allarmeLivello: {"allarmeLivello":0/1}
            "allarmeLivello" -> {
                val v = json["allarmeLivello"].asInt
                val old = stateManager.state.value.alarmLevel

                if (v == 1 && old != 1) {
                    updateNotification(getString(R.string.alarm_low_food), true)
                }

                stateManager.updateLevelAlarm(v)
            }

            // allarmeBatteria: {"allarmeBatteria":0/1}
            "allarmeBatteria" -> {
                val v = json["allarmeBatteria"].asInt
                val old = stateManager.state.value.alarmBattery

                if (v == 1 && old != 1) {
                    updateNotification(getString(R.string.alarm_low_battery), true)
                }

                stateManager.updateBatteryAlarm(v)
            }

            else -> {
                Log.w("DispenserService", "Unknown SSE event: $eventName, data: $data")
            }
        }
    }

    // -----------------------------
    // NOTIFICATIONS
    // -----------------------------

    private fun createNotification(content: String, isAlert: Boolean): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, if (isAlert) CHANNEL_ID_ALERTS else CHANNEL_ID_STATUS)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(content)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(!isAlert)
            .setAutoCancel(isAlert)
            .build()
    }

    private fun updateNotification(content: String, isAlert: Boolean) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(
            if (isAlert) NOTIFICATION_ID_ALERT else NOTIFICATION_ID,
            createNotification(content, isAlert)
        )
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID_STATUS,
                    getString(R.string.status_channel_name),
                    NotificationManager.IMPORTANCE_LOW
                )
            )

            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID_ALERTS,
                    getString(R.string.alert_channel_name),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    enableLights(true)
                    enableVibration(true)
                }
            )
        }
    }

    override fun onDestroy() {
        stopSse()
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID_STATUS = "dispenser_channel_status"
        private const val CHANNEL_ID_ALERTS = "dispenser_channel_alerts"
        private const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_ID_ALERT = 2
    }
}
