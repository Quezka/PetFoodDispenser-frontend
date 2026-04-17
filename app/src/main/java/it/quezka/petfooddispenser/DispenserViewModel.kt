package it.quezka.petfooddispenser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.sse.EventSource
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
    val isTestTimeActive: Boolean = false
)

@HiltViewModel
class DispenserViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val networkManagerFactory: NetworkManagerFactory
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var networkManager: NetworkManager? = null
    private var sseConnection: EventSource? = null
    private val gson = Gson()

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
                    if (!_uiState.value.waitingForManualAction) {
                        refresh()
                    }
                } else {
                    disconnectSse()
                    networkManager = null
                }
            }
        }
        viewModelScope.launch {
            settingsRepository.testMode.collect { enabled ->
                _uiState.update { it.copy(isTestModeEnabled = enabled) }
            }
        }
    }

    fun updateTestMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateTestMode(enabled)
        }
    }

    fun toggleTestTime(isTestTime: Boolean) {
        val manager = networkManager ?: return
        val value = if (isTestTime) "1" else "0"
        
        _uiState.update { it.copy(isTestTimeActive = isTestTime) }
        
        viewModelScope.launch {
            manager.sendCommand("set", "test_time", value)
        }
    }

    fun manualErogate() {
        val manager = networkManager ?: return
        viewModelScope.launch {
            manager.sendCommand("set", "erogate", "1")
        }
    }

    private fun ensureSseConnected() {
        if (sseConnection != null) return
        val manager = networkManager ?: return

        sseConnection = manager.startSse(
            onMessage = { json ->
                updateStateFromJson(json)
            },
            onError = { error ->
                _uiState.update { 
                    it.copy(
                        isConnected = false,
                        error = "Connection Lost: ${error.message ?: "Unknown Error"}",
                        waitingForManualAction = true
                    )
                }
                disconnectSse()
            }
        )
    }

    private fun disconnectSse() {
        sseConnection?.cancel()
        sseConnection = null
    }

    private fun updateStateFromJson(json: String) {
        try {
            var state = gson.fromJson(json, DispenserState::class.java)
            
            if (state.mode == "remote") {
                if (state.cr1Remote == 0f) { updateRemoteValue(1, 1f); state = state.copy(cr1Remote = 1f) }
                if (state.cr2Remote == 0f) { updateRemoteValue(2, 1f); state = state.copy(cr2Remote = 1f) }
                if (state.cr3Remote == 0f) { updateRemoteValue(3, 1f); state = state.copy(cr3Remote = 1f) }
            }

            _uiState.update { 
                it.copy(
                    dispenserState = state,
                    isConnected = true,
                    isProbing = false,
                    lastRawJson = json,
                    waitingForManualAction = false
                )
            }
        } catch (e: Exception) {
            // Log parsing errors
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
                updateStateFromJson(json)
                ensureSseConnected()
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
        
        // Capture current physical positions if we are switching TO remote
        val currentState = _uiState.value.dispenserState
        
        viewModelScope.launch {
            // 1. If switching to remote, sync remote values with physical ones first
            if (isRemote) {
                manager.sendCommand("set", "cr1_r", currentState.cr1.toInt().toString())
                manager.sendCommand("set", "cr2_r", currentState.cr2.toInt().toString())
                manager.sendCommand("set", "cr3_r", currentState.cr3.toInt().toString())
            }

            // 2. Then switch the mode
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

    override fun onCleared() {
        super.onCleared()
        disconnectSse()
    }
}
