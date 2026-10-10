package com.shopmind.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopmind.data.local.entity.UserEntity
import com.shopmind.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _authResult = MutableSharedFlow<Result<Unit>>()
    val authResult: SharedFlow<Result<Unit>> = _authResult.asSharedFlow()

    fun login(phoneNumber: String, password: String) {
        viewModelScope.launch {
            val result = authRepository.login(phoneNumber, password)
            _authResult.emit(result)
        }
    }

    fun signup(username: String, phoneNumber: String, password: String) {
        viewModelScope.launch {
            val user = UserEntity(
                username = username,
                phoneNumber = phoneNumber,
                password = password
            )
            val result = authRepository.signup(user)
            _authResult.emit(result)
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
