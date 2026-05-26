package com.example.goldencinema

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ScreeningsUiState {
    object Loading : ScreeningsUiState()
    data class Success(val screenings: List<ScreeningDto>) : ScreeningsUiState()
    data class Error(val message: String) : ScreeningsUiState()
}

class ScreeningViewModel : ViewModel() {
    private val _state = MutableStateFlow<ScreeningsUiState>(ScreeningsUiState.Loading)
    val state: StateFlow<ScreeningsUiState> = _state.asStateFlow()

    init {
        loadScreenings()
    }

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
