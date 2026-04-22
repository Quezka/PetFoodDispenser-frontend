package it.quezka.petfooddispenser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val isTestModeEnabled: Boolean = false
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
            settingsRepository.serverIp.collect { ip ->
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
            stateManager.state.collect { state ->
                _uiState.update { it.copy(dispenserState = state) }
            }
        }

        viewModelScope.launch {
            stateManager.isConnected.collect { connected ->
                _uiState.update { it.copy(isConnected = connected) }
            }
        }
    }

    fun updateTestMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateTestMode(enabled)
            val manager = networkManager ?: return@launch
            val value = if (enabled) "1" else "0"
            manager.sendCommand("set", "test", value)
        }
    }

    fun manualErogate() {
        val manager = networkManager ?: return
        viewModelScope.launch {
            manager.sendCommand("set", "erogate", "1")
        }
    }

    fun updateServerIp(ip: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(waitingForManualAction = false) }
            settingsRepository.updateServerIp(ip)
        }
    }

    fun refresh() {
        val manager = networkManager ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isProbing = true, error = null, waitingForManualAction = false) }
            
            manager.fetchStatus().onSuccess { json ->
                try {
                    val gson = com.google.gson.Gson()
                    val state = gson.fromJson(json, DispenserState::class.java)
                    stateManager.updateState(state)
                    stateManager.setConnected(true)
                } catch (e: Exception) {
                    // Ignore parsing errors
                }
                _uiState.update { it.copy(isProbing = false) }
            }.onFailure { e ->
                _uiState.update { 
                    it.copy(
                        isConnected = false, 
                        isProbing = false, 
                        error = e.message,
                        waitingForManualAction = true 
                    ) 
                }
            }
        }
    }

    fun setMode(isRemote: Boolean) {
        val manager = networkManager ?: return
        val modeStr = if (isRemote) "remote" else "local"
        val currentState = _uiState.value.dispenserState
        
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
                manager.sendCommand("set", "cr1_r", currentState.cr1.toInt().toString())
                manager.sendCommand("set", "cr2_r", currentState.cr2.toInt().toString())
                manager.sendCommand("set", "cr3_r", currentState.cr3.toInt().toString())
            }
            manager.sendCommand("set", "mode", modeStr).onSuccess {
                delay(300)
                refresh()
            }
        }
    }

    fun updateRemoteValue(index: Int, value: Float) {
        val manager = networkManager ?: return
        val key = "cr${index}_r"
        
        _uiState.update { current ->
            val newState = when(index) {
                1 -> current.dispenserState.copy(cr1Remote = value)
                2 -> current.dispenserState.copy(cr2Remote = value)
                3 -> current.dispenserState.copy(cr3Remote = value)
                else -> current.dispenserState
            }
            current.copy(dispenserState = newState)
        }

        viewModelScope.launch {
            manager.sendCommand("set", key, value.toInt().toString())
        }
    }

    fun setDebug(enabled: Boolean) {
        _uiState.update { it.copy(showDebug = enabled) }
    }
}
