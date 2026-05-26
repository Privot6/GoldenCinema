package com.example.goldencinema

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object Loading : RegisterUiState()
    object Success : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}

class RegisterViewModel : ViewModel() {
    private val _state = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val state: StateFlow<RegisterUiState> = _state.asStateFlow()

    fun register(firstName: String, lastName: String, email: String, password: String) {
        viewModelScope.launch {
            _state.value = RegisterUiState.Loading
            try {
                val response = NetworkModule.authApi.register(
                    RegisterRequest(firstName.trim(), lastName.trim(), email.trim(), password)
                )
                if (response.isSuccessful) {
                    val token = response.body()?.token
                    if (token != null) {
                        TokenStore.save(token)
                        _state.value = RegisterUiState.Success
                    } else {
                        _state.value = RegisterUiState.Error("Brak tokenu w odpowiedzi serwera")
                    }
                } else {
                    val errorMsg = try {
                        JSONObject(response.errorBody()?.string() ?: "").optString("error", "")
                            .ifEmpty { null }
                    } catch (_: Exception) { null }
                    _state.value = RegisterUiState.Error(
                        errorMsg ?: when (response.code()) {
                            409 -> "Ten adres email jest już zajęty"
                            else -> "Błąd serwera: ${response.code()}"
                        }
                    )
                }
            } catch (e: Exception) {
                _state.value = RegisterUiState.Error("Błąd połączenia z serwerem")
            }
        }
    }

    fun resetState() {
        _state.value = RegisterUiState.Idle
    }
}
