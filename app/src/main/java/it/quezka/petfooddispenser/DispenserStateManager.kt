package it.quezka.petfooddispenser

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DispenserStateManager @Inject constructor() {
    private val _state = MutableStateFlow(DispenserState())
    val state = _state.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    private val _isErogating = MutableStateFlow(false)
    val isErogating = _isErogating.asStateFlow()

    /**
     * Aggiorna lo stato generale preservando gli allarmi.
     * Gli allarmi vengono resettati a 0 solo tramite i metodi specifici updateLevelAlarm/updateBatteryAlarm.
     */
    fun updateState(newState: DispenserState) {
        _state.update { current ->
            newState.copy(
                alarmLevel = if (newState.alarmLevel == 1) 1 else current.alarmLevel,
                alarmBattery = if (newState.alarmBattery == 1) 1 else current.alarmBattery,
                alarms = if (newState.alarms.isNotBlank()) newState.alarms else current.alarms
            )
        }
    }

    fun updateLevelAlarm(value: Int) {
        _state.update { it.copy(alarmLevel = value) }
    }

    fun updateBatteryAlarm(value: Int) {
        _state.update { it.copy(alarmBattery = value) }
    }

    fun setConnected(connected: Boolean) {
        _isConnected.value = connected
    }

    fun setErogating(erogating: Boolean) {
        _isErogating.value = erogating
    }
}
