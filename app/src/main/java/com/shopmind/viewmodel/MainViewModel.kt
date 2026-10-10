package com.shopmind.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopmind.data.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

sealed interface StartupState {

    data object Loading : StartupState

    data object Authenticated : StartupState

    data object Unauthenticated : StartupState
}

class MainViewModel(private val authRepository: AuthRepository): ViewModel() {
    private val _state = MutableStateFlow<StartupState>(StartupState.Loading)
    val state: StateFlow<StartupState> = _state.asStateFlow()

    init {
        initialize()
    }

    private fun initialize() {
        viewModelScope.launch {
            authRepository.isLoggedIn.collect { isAuthenticated ->
                if (_state.value == StartupState.Loading) {
                    delay(1000.milliseconds)
                }

                _state.value =
                    if (isAuthenticated) {
                        StartupState.Authenticated
                    } else {
                        StartupState.Unauthenticated
                    }
            }
        }
    }
}
