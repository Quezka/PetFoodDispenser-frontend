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

    private fun restartSse() {
        stopSse()
        startSse()
    }

    private fun startSse() {
        sseConnection?.cancel()
        val manager = networkManager ?: return

        sseConnection = manager.startSse(
            onOpen = {
                Log.d("DispenserService", "SSE Connected")
                isSseConnected = true
                stateManager.setConnected(true)
                updateNotification(getString(R.string.notification_connected), false)
                stopReconnectLoop()
            },
            onEvent = { eventName, data ->
                Log.d("DispenserService", "SSE Event: $eventName Data: $data")
                if (!isSseConnected) {
                    isSseConnected = true
                    stateManager.setConnected(true)
                    stopReconnectLoop()
                }
                handleSseEvent(data)
            },
            onError = { t ->
                Log.e("DispenserService", "SSE Error", t)
                isSseConnected = false
                stateManager.setConnected(false)
                updateNotification(getString(R.string.notification_lost), false)
                startReconnectLoop()
            }
        )
    }

    private fun stopSse() {
        isSseConnected = false
        sseConnection?.cancel()
        sseConnection = null
        stateManager.setConnected(false)
        stopReconnectLoop()
    }

    private fun startReconnectLoop() {
        if (reconnectJob?.isActive == true || currentIp.isBlank()) return
        reconnectJob = lifecycleScope.launch {
            var delayMs = 2000L
            while (!isSseConnected && currentIp.isNotBlank()) {
                delay(delayMs)
                if (isSseConnected) break
                Log.d("DispenserService", "Retrying SSE connection...")
                startSse()
                delayMs = (delayMs * 2).coerceAtMost(60000L)
            }
        }
    }

    private fun stopReconnectLoop() {
        reconnectJob?.cancel()
        reconnectJob = null
    }

    private fun handleSseEvent(data: String) {
        try {
            val jsonElement = JsonParser.parseString(data)
            if (!jsonElement.isJsonObject) return
            val json = jsonElement.asJsonObject

            // 1. Alarms (Prioritized)
            if (json.has("allarmeLivello")) {
                val v = json["allarmeLivello"].asInt
                if (v == 1 && stateManager.state.value.alarmLevel != 1) {
                    updateNotification(getString(R.string.alarm_low_food), true)
                }
                stateManager.updateLevelAlarm(v)
            }
            if (json.has("allarmeBatteria")) {
                val v = json["allarmeBatteria"].asInt
                if (v == 1 && stateManager.state.value.alarmBattery != 1) {
                    updateNotification(getString(R.string.alarm_low_battery), true)
                }
                stateManager.updateBatteryAlarm(v)
            }

            // 2. Erogation
            if (json.has("is_erogating")) {
                stateManager.setErogating(json["is_erogating"].asInt == 1)
            }

            // 3. General State Update (Merge fields)
            val current = stateManager.state.value
            var updated = current

            if (json.has("cr1")) updated = updated.copy(cr1 = json["cr1"].asFloat)
            if (json.has("cr2")) updated = updated.copy(cr2 = json["cr2"].asFloat)
            if (json.has("cr3")) updated = updated.copy(cr3 = json["cr3"].asFloat)
            if (json.has("mode")) updated = updated.copy(mode = json["mode"].asString)
            if (json.has("alarms")) updated = updated.copy(alarms = json["alarms"].asString)
            if (json.has("test")) {
                val t = json["test"]
                val isTest = if (t.isJsonPrimitive && t.asJsonPrimitive.isNumber) t.asInt == 1 else t.asBoolean
                updated = updated.copy(testMode = isTest)
            }

            if (updated != current) {
                stateManager.updateState(updated)
            }
        } catch (e: Exception) {
            Log.e("DispenserService", "Parse error: $data", e)
        }
    }

    private fun createNotification(content: String, isAlert: Boolean): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
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
        manager.notify(if (isAlert) NOTIFICATION_ID_ALERT else NOTIFICATION_ID, createNotification(content, isAlert))
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(NotificationChannel(CHANNEL_ID_STATUS, getString(R.string.status_channel_name), NotificationManager.IMPORTANCE_LOW))
            manager.createNotificationChannel(NotificationChannel(CHANNEL_ID_ALERTS, getString(R.string.alert_channel_name), NotificationManager.IMPORTANCE_HIGH).apply {
                enableLights(true)
                enableVibration(true)
            })
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