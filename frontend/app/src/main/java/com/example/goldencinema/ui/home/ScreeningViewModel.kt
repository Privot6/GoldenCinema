package com.example.goldencinema

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Stan ekranu repertuaru. */
sealed class ScreeningsUiState {
    /** Trwa pobieranie danych. */
    object Loading : ScreeningsUiState()
    /** Dane załadowane pomyślnie. */
    data class Success(val screenings: List<ScreeningDto>) : ScreeningsUiState()
    /** Błąd pobierania danych z opisem przyczyny. */
    data class Error(val message: String) : ScreeningsUiState()
}

/**
 * ViewModel ekranu repertuaru. Pobiera listę nadchodzących seansów przy inicjalizacji.
 *
 * @property state aktualny stan ekranu repertuaru
 */
class ScreeningViewModel : ViewModel() {
    private val _state = MutableStateFlow<ScreeningsUiState>(ScreeningsUiState.Loading)
    val state: StateFlow<ScreeningsUiState> = _state.asStateFlow()

    init {
        loadScreenings()
    }

    /** Pobiera listę nadchodzących seansów z API. */
    fun loadScreenings() {
        viewModelScope.launch {
            _state.value = ScreeningsUiState.Loading
            try {
                val screenings = NetworkModule.screeningApi.getScreenings()
                _state.value = ScreeningsUiState.Success(screenings)
            } catch (e: Exception) {
                _state.value = ScreeningsUiState.Error(e.message ?: "Błąd pobierania repertuaru")
            }
        }
    }
}
