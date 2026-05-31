package com.example.goldencinema

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Stan ekranu edycji seansu. */
sealed class ScreeningEditUiState {
    object Loading : ScreeningEditUiState()
    data class Loaded(val screening: ScreeningDto) : ScreeningEditUiState()
    data class Error(val message: String) : ScreeningEditUiState()
    object Saving : ScreeningEditUiState()
    object Saved : ScreeningEditUiState()
}

/**
 * ViewModel ekranu edycji seansu dla pracownika.
 * Pobiera dane seansu i umożliwia ich aktualizację przez API.
 */
class ScreeningEditViewModel : ViewModel() {

    private val _state = MutableStateFlow<ScreeningEditUiState>(ScreeningEditUiState.Loading)
    val state: StateFlow<ScreeningEditUiState> = _state.asStateFlow()

    /** Pobiera dane seansu o podanym identyfikatorze. */
    fun loadScreening(id: Long) {
        viewModelScope.launch {
            _state.value = ScreeningEditUiState.Loading
            try {
                val screening = NetworkModule.employeeScreeningApi.getScreening(id)
                _state.value = ScreeningEditUiState.Loaded(screening)
            } catch (e: Exception) {
                _state.value = ScreeningEditUiState.Error(e.message ?: "Błąd pobierania seansu")
            }
        }
    }

    /**
     * Zapisuje zaktualizowane dane seansu.
     *
     * @param id         identyfikator seansu
     * @param movieId    identyfikator filmu (bez zmiany)
     * @param hallId     identyfikator sali (bez zmiany)
     * @param startTime  nowy czas rozpoczęcia ISO 8601
     * @param endTime    nowy czas zakończenia ISO 8601
     * @param basePrice  nowa cena bazowa jako tekst
     */
    fun save(
        id: Long,
        movieId: Long,
        hallId: Long,
        startTime: String,
        endTime: String,
        basePrice: String
    ) {
        val price = basePrice.replace(",", ".").toDoubleOrNull() ?: return
        viewModelScope.launch {
            _state.value = ScreeningEditUiState.Saving
            try {
                NetworkModule.employeeScreeningApi.updateScreening(
                    id,
                    UpdateScreeningRequest(movieId, hallId, startTime, endTime, price)
                )
                _state.value = ScreeningEditUiState.Saved
            } catch (e: Exception) {
                _state.value = ScreeningEditUiState.Error(e.message ?: "Błąd zapisu")
            }
        }
    }
}
