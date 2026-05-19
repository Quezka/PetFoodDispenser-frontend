package it.quezka.petfooddispenser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UiState(
    val dispenserState: DispenserState = DispenserState(),
    val isConnected: Boolean = false,
    val isProbing: Boolean = false,
    val error: String? = null,
    val lastRawJson: String? = null,
    val showDebug: Boolean = false,
    val isSetupRequired: Boolean = false,
    val currentServerIp: String = "",
    val waitingForManualAction: Boolean = false,
    val isTestModeEnabled: Boolean = false,
    val prolungheSerbatoio: Int = 0,
    val volumeMin: Int = 0,
    val isFoodDispenser: Boolean = true,
    val isErogating: Boolean = false,
    val isSavingSettings: Boolean = false
)

@HiltViewModel
class DispenserViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val networkManagerFactory: NetworkManagerFactory,
    private val stateManager: DispenserStateManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var networkManager: NetworkManager? = null

    init {
        viewModelScope.launch {
            settingsRepository.serverIp
                .distinctUntilChanged()
                .collect { ip ->
                val setupRequired = ip.isBlank()
                _uiState.update { 
                    it.copy(
                        currentServerIp = ip, 
                        isSetupRequired = setupRequired,
                        waitingForManualAction = setupRequired
                    ) 
                }
                
                if (!setupRequired) {
                    networkManager = networkManagerFactory(ip)
                    refresh()
                } else {
                    networkManager = null
                }
            }
        }

        viewModelScope.launch {
            settingsRepository.testMode.collect { enabled ->
                _uiState.update { it.copy(isTestModeEnabled = enabled) }
            }
        }

        viewModelScope.launch {
            settingsRepository.prolungheSerbatoio.collect { count ->
                _uiState.update { it.copy(prolungheSerbatoio = count) }
            }
        }

        viewModelScope.launch {
            settingsRepository.volumeMin.collect { volume ->
                _uiState.update { it.copy(volumeMin = volume) }
            }
        }

        viewModelScope.launch {
            settingsRepository.tipoDispenser.collect { isFood ->
                _uiState.update { it.copy(isFoodDispenser = isFood) }
            }
        }

        viewModelScope.launch {
            stateManager.state.collect { state ->
                _uiState.update { it.copy(dispenserState = state) }
            }
        }

        viewModelScope.launch {
            stateManager.isConnected.collect { connected ->
                _uiState.update { it.copy(isConnected = connected) }
            }
        }

        viewModelScope.launch {
            stateManager.isErogating.collect { erogating ->
                _uiState.update { it.copy(isErogating = erogating) }
            }
        }
    }

    fun saveAllSettings(
        ip: String,
        testMode: Boolean,
        prolunghe: Int,
        volumeMin: Int,
        isFood: Boolean,
        showDebug: Boolean
    ) {
        viewModelScope.launch {
            // Mostriamo la rotellina
            _uiState.update { it.copy(isSavingSettings = true) }

            val ipChanged = ip != _uiState.value.currentServerIp
            
            settingsRepository.updateAllSettings(ip, testMode, prolunghe, volumeMin, isFood)
            _uiState.update { it.copy(showDebug = showDebug) }

            if (ipChanged) {
                delay(1500)
            }

            val manager = networkManager ?: run {
                _uiState.update { it.copy(isSavingSettings = false) }
                return@launch
            }
            
            val state = _uiState.value.dispenserState

            val commands = listOf(
                "test" to (if (testMode) "1" else "0"),
                "prolunghe_serbatoio" to prolunghe.toString(),
                "volume_min" to volumeMin.toString(),
                "tipo_dispenser" to (if (isFood) "1" else "0"),
                "mode" to state.mode,
                "cr1_r" to state.cr1Remote.toInt().toString(),
                "cr2_r" to state.cr2Remote.toInt().toString(),
                "cr3_r" to state.cr3Remote.toInt().toString()
            )

            commands.forEach { (variable, value) ->
                manager.sendCommand("set", variable, value)
                delay(400)
            }

            delay(500)
            
            // Eseguiamo il refresh e aspettiamo che finisca
            refreshInternal().join()
            
            // Nascondiamo la rotellina solo dopo il refresh
            _uiState.update { it.copy(isSavingSettings = false) }
        }
    }

    // Refactored refresh to be callable and awaitable
    private fun refreshInternal(): Job {
        val manager = networkManager ?: return viewModelScope.launch {}
        return viewModelScope.launch {
            _uiState.update { it.copy(isProbing = true, error = null, waitingForManualAction = false) }
            manager.fetchStatus().onSuccess { json ->
                try {
                    val gson = com.google.gson.Gson()
                    val state = gson.fromJson(json, DispenserState::class.java)
                    stateManager.updateState(state)
                    stateManager.setConnected(true)
                } catch (e: Exception) {}
                _uiState.update { it.copy(isProbing = false) }
            }.onFailure { e ->
                _uiState.update { 
                    it.copy(isConnected = false, isProbing = false, error = e.message, waitingForManualAction = true) 
                }
                stateManager.setConnected(false)
            }
        }
    }

    fun refresh() {
        refreshInternal()
    }

    fun updateTestMode(enabled: Boolean) {
        if (_uiState.value.isTestModeEnabled == enabled) return
        viewModelScope.launch {
            settingsRepository.updateTestMode(enabled)
            val manager = networkManager ?: return@launch
            manager.sendCommand("set", "test", if (enabled) "1" else "0")
        }
    }

    fun updateProlungheSerbatoio(count: Int) {
        if (_uiState.value.prolungheSerbatoio == count) return
        viewModelScope.launch {
            settingsRepository.updateProlungheSerbatoio(count)
            val manager = networkManager ?: return@launch
            manager.sendCommand("set", "prolunghe_serbatoio", count.toString())
        }
    }

    fun updateVolumeMin(volume: Int) {
        if (_uiState.value.volumeMin == volume) return
        viewModelScope.launch {
            settingsRepository.updateVolumeMin(volume)
            val manager = networkManager ?: return@launch
            manager.sendCommand("set", "volume_min", volume.toString())
        }
    }

    fun updateTipoDispenser(isFood: Boolean) {
        if (_uiState.value.isFoodDispenser == isFood) return
        viewModelScope.launch {
            settingsRepository.updateTipoDispenser(isFood)
            val manager = networkManager ?: return@launch
            manager.sendCommand("set", "tipo_dispenser", if (isFood) "1" else "0")
        }
    }

    fun manualErogate() {
        val manager = networkManager ?: return
        if (_uiState.value.isErogating || !_uiState.value.isConnected) return

        viewModelScope.launch {
            _uiState.update { it.copy(isErogating = true) }
            manager.sendCommand("set", "erogate", "1").onFailure {
                _uiState.update { it.copy(isErogating = false) }
            }
        }
    }

    fun updateServerIp(ip: String) {
        if (_uiState.value.currentServerIp == ip) return
        viewModelScope.launch {
            _uiState.update { it.copy(waitingForManualAction = false) }
            settingsRepository.updateServerIp(ip)
        }
    }

    fun setMode(isRemote: Boolean) {
        val manager = networkManager ?: return
        val modeStr = if (isRemote) "remote" else "local"
        val currentState = _uiState.value.dispenserState
        
        if (modeStr == currentState.mode) return

        _uiState.update { current ->
            current.copy(
                dispenserState = current.dispenserState.copy(
                    mode = modeStr,
                    cr1Remote = if (isRemote) currentState.cr1 else current.dispenserState.cr1Remote,
                    cr2Remote = if (isRemote) currentState.cr2 else current.dispenserState.cr2Remote,
                    cr3Remote = if (isRemote) currentState.cr3 else current.dispenserState.cr3Remote
                )
            )
        }
        
        viewModelScope.launch {
            if (isRemote) {
                if (currentState.cr1.toInt() != currentState.cr1Remote.toInt() && currentState.cr1 > 1f) {
                    manager.sendCommand("set", "cr1_r", currentState.cr1.toInt().toString())
                }
                if (currentState.cr2.toInt() != currentState.cr2Remote.toInt()) {
                    manager.sendCommand("set", "cr2_r", currentState.cr2.toInt().toString())
                }
                if (currentState.cr3.toInt() != currentState.cr3Remote.toInt()) {
                    manager.sendCommand("set", "cr3_r", currentState.cr3.toInt().toString())
                }
            }
            manager.sendCommand("set", "mode", modeStr).onSuccess {
                delay(300)
                refresh()
            }
        }
    }

    private var debounceJobs = mutableMapOf<Int, Job>()
    fun updateRemoteValue(index: Int, value: Float) {
        val manager = networkManager ?: return
        val key = "cr${index}_r"
        val intValue = value.toInt()
        val currentState = _uiState.value.dispenserState
        val previousValue = when(index) {
            1 -> currentState.cr1Remote
            2 -> currentState.cr2Remote
            3 -> currentState.cr3Remote
            else -> return
        }

        _uiState.update { current ->
            val newState = when(index) {
                1 -> current.dispenserState.copy(cr1Remote = value)
                2 -> current.dispenserState.copy(cr2Remote = value)
                3 -> current.dispenserState.copy(cr3Remote = value)
                else -> current.dispenserState
            }
            current.copy(dispenserState = newState)
        }

        if (intValue == previousValue.toInt()) return

        debounceJobs[index]?.cancel()
        debounceJobs[index] = viewModelScope.launch {
            delay(150)
            manager.sendCommand("set", key, intValue.toString())
        }
    }

    fun setDebug(enabled: Boolean) {
        _uiState.update { it.copy(showDebug = enabled) }
    }
}
