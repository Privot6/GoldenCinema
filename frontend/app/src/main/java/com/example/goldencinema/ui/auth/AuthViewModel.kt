package com.example.goldencinema

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class AuthViewModel : ViewModel() {
    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginUiState.Loading
            try {
                val response = NetworkModule.authApi.login(LoginRequest(email.trim(), password))
                if (response.isSuccessful) {
                    val token = response.body()?.token
                    if (token != null) {
                        TokenStore.save(token)
                        _loginState.value = LoginUiState.Success
                    } else {
                        _loginState.value = LoginUiState.Error("Brak tokenu w odpowiedzi serwera")
                    }
                } else {
                    _loginState.value = LoginUiState.Error(
                        if (response.code() == 401) "Nieprawidłowy email lub hasło"
                        else "Błąd serwera: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _loginState.value = LoginUiState.Error("Błąd połączenia z serwerem")
            }
        }
    }

    fun resetState() {
        _loginState.value = LoginUiState.Idle
    }
}
