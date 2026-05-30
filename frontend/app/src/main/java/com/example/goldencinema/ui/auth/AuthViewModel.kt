package com.example.goldencinema

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Stan ekranu logowania. */
sealed class LoginUiState {
    /** Ekran bezczynny — przed pierwszą próbą logowania. */
    object Idle : LoginUiState()
    /** Trwa żądanie do serwera. */
    object Loading : LoginUiState()
    /** Logowanie zakończone sukcesem — token zapisany w [TokenStore]. */
    object Success : LoginUiState()
    /** Błąd logowania z opisem przyczyny. */
    data class Error(val message: String) : LoginUiState()
}

/**
 * ViewModel ekranu logowania. Zarządza procesem uwierzytelniania i przechowuje stan UI.
 *
 * @property loginState aktualny stan procesu logowania
 */
class AuthViewModel : ViewModel() {
    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    /**
     * Inicjuje proces logowania. Zapisuje token w [TokenStore] po sukcesie.
     *
     * @param email    adres email użytkownika
     * @param password hasło użytkownika
     */
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

    /** Resetuje stan do [LoginUiState.Idle] — np. po opuszczeniu ekranu logowania. */
    fun resetState() {
        _loginState.value = LoginUiState.Idle
    }
}
