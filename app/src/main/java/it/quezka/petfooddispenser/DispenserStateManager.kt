package it.quezka.petfooddispenser

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DispenserStateManager @Inject constructor() {
    private val _state = MutableStateFlow(DispenserState())
    val state = _state.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    fun updateState(newState: DispenserState) {
        _state.value = newState
    }

    fun setConnected(connected: Boolean) {
        _isConnected.value = connected
    }
}
