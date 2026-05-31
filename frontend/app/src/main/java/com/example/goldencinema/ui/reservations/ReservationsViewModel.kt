package com.example.goldencinema

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Stan ekranu listy rezerwacji użytkownika. */
sealed class ReservationsUiState {
    /** Trwa pobieranie danych. */
    object Loading : ReservationsUiState()
    /** Dane załadowane pomyślnie. */
    data class Success(val reservations: List<ReservationResponseDto>) : ReservationsUiState()
    /** Błąd pobierania danych. */
    data class Error(val message: String) : ReservationsUiState()
}

/** Zdarzenie jednorazowe inicjujące płatność. */
sealed class CheckoutEvent {
    /** Brak aktywnego zdarzenia. */
    object Idle : CheckoutEvent()
    /** Trwa pobieranie URL płatności. */
    object Loading : CheckoutEvent()
    /** URL do sesji płatności Stripe gotowy do otwarcia. */
    data class OpenUrl(val url: String) : CheckoutEvent()
    /** Błąd podczas pobierania URL płatności. */
    data class Error(val message: String) : CheckoutEvent()
}

/**
 * ViewModel ekranu rezerwacji użytkownika. Obsługuje listę rezerwacji i inicjowanie płatności.
 *
 * @property state         aktualny stan listy rezerwacji
 * @property isRefreshing  czy trwa odświeżanie pull-to-refresh
 * @property checkoutEvent jednorazowe zdarzenie płatności
 */
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

    /** Pobiera listę rezerwacji zalogowanego użytkownika z API. */
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

    /** Odświeża listę rezerwacji (pull-to-refresh) bez pokazywania głównego loadera. */
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

    /**
     * Pobiera URL płatności Stripe dla podanej rezerwacji i emituje zdarzenie [CheckoutEvent.OpenUrl].
     *
     * @param reservationId identyfikator rezerwacji do opłacenia
     */
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

    /** Resetuje zdarzenie płatności do [CheckoutEvent.Idle] po obsłużeniu przez UI. */
    fun resetCheckoutEvent() {
        _checkoutEvent.value = CheckoutEvent.Idle
    }

    /** Anuluje rezerwację użytkownika i odświeża listę po sukcesie. */
    fun cancelReservation(reservationId: Long) {
        viewModelScope.launch {
            try {
                val response = NetworkModule.reservationApi.cancelReservation(reservationId)
                if (response.isSuccessful) {
                    loadMyReservations()
                }
            } catch (_: Exception) { }
        }
    }
}
