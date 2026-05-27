package com.example.goldencinema

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ReservationsUiState {
    object Loading : ReservationsUiState()
    data class Success(val reservations: List<ReservationResponseDto>) : ReservationsUiState()
    data class Error(val message: String) : ReservationsUiState()
}

sealed class CheckoutEvent {
    object Idle : CheckoutEvent()
    object Loading : CheckoutEvent()
    data class OpenUrl(val url: String) : CheckoutEvent()
    data class Error(val message: String) : CheckoutEvent()
}

class ReservationsViewModel : ViewModel() {
    private val _state = MutableStateFlow<ReservationsUiState>(ReservationsUiState.Loading)
    val state: StateFlow<ReservationsUiState> = _state.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _checkoutEvent = MutableStateFlow<CheckoutEvent>(CheckoutEvent.Idle)
    val checkoutEvent: StateFlow<CheckoutEvent> = _checkoutEvent.asStateFlow()

    init {
        loadMyReservations()
    }

    fun loadMyReservations() {
        viewModelScope.launch {
            _state.value = ReservationsUiState.Loading
            try {
                val reservations = NetworkModule.reservationApi.getMyReservations()
                _state.value = ReservationsUiState.Success(reservations)
            } catch (e: Exception) {
                _state.value = ReservationsUiState.Error(e.message ?: "Błąd pobierania rezerwacji")
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val reservations = NetworkModule.reservationApi.getMyReservations()
                _state.value = ReservationsUiState.Success(reservations)
            } catch (e: Exception) {
                _state.value = ReservationsUiState.Error(e.message ?: "Błąd pobierania rezerwacji")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun startPayment(reservationId: Long) {
        viewModelScope.launch {
            _checkoutEvent.value = CheckoutEvent.Loading
            try {
                val response = NetworkModule.reservationApi.getCheckoutUrl(reservationId)
                when {
                    response.isSuccessful -> {
                        val url = response.body()?.paymentUrl
                        if (url != null) {
                            _checkoutEvent.value = CheckoutEvent.OpenUrl(url)
                        } else {
                            _checkoutEvent.value = CheckoutEvent.Error("Brak URL płatności")
                        }
                    }
                    else -> _checkoutEvent.value = CheckoutEvent.Error("Błąd serwera: ${response.code()}")
                }
            } catch (e: Exception) {
                _checkoutEvent.value = CheckoutEvent.Error(e.message ?: "Błąd połączenia")
            }
        }
    }

    fun resetCheckoutEvent() {
        _checkoutEvent.value = CheckoutEvent.Idle
    }
}
