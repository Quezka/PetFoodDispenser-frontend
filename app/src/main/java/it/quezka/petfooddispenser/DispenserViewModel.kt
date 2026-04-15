package it.quezka.petfooddispenser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
    val currentServerIp: String = ""
)

@HiltViewModel
class DispenserViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val networkManagerFactory: NetworkManagerFactory
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var networkManager: NetworkManager? = null
    private val gson = Gson()

    init {
        viewModelScope.launch {
            settingsRepository.serverIp.collect { ip ->
                _uiState.update { it.copy(currentServerIp = ip, isSetupRequired = ip.isBlank()) }
                if (ip.isNotBlank()) {
                    networkManager = networkManagerFactory(ip)
                    refresh()
                } else {
                    networkManager = null
                }
            }
        }
    }

    fun updateServerIp(ip: String) {
        viewModelScope.launch {
            settingsRepository.updateServerIp(ip)
        }
    }

    fun refresh() {
        val manager = networkManager ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isProbing = true, error = null) }
            
            manager.fetchStatus().onSuccess { json ->
                try {
                    var state = gson.fromJson(json, DispenserState::class.java)
                    
                    // Handle 0 values in remote mode - Auto correction
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
                            lastRawJson = json
                        )
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(isProbing = false, error = "Parse Error: ${e.message}") }
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isConnected = false, isProbing = false, error = e.message) }
            }
        }
    }

    fun setMode(isRemote: Boolean) {
        val manager = networkManager ?: return
        val modeStr = if (isRemote) "remote" else "local"
        
        // Optimistic UI update for mode
        _uiState.update { it.copy(dispenserState = it.dispenserState.copy(mode = modeStr)) }

        viewModelScope.launch {
            manager.sendCommand("set", "mode", modeStr).onSuccess {
                delay(300)
                refresh()
                delay(1000)
                refresh()
            }
        }
    }

    fun updateRemoteValue(index: Int, value: Float) {
        val manager = networkManager ?: return
        val key = "cr${index}_r"
        
        // Optimistic update
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
