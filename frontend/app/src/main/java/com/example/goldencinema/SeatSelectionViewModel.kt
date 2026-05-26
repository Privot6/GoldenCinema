package com.example.goldencinema

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SeatsUiState {
    object Loading : SeatsUiState()
    data class Success(val rows: List<SeatRowDto>) : SeatsUiState()
    data class Error(val message: String) : SeatsUiState()
}

sealed class ReservationUiState {
    object Idle : ReservationUiState()
    object Loading : ReservationUiState()
    data class Success(val code: String) : ReservationUiState()
    data class Conflict(val message: String) : ReservationUiState()
    data class Error(val message: String) : ReservationUiState()
}

class SeatSelectionViewModel(
    private val screeningId: Long,
    private val basePrice: Double
) : ViewModel() {

    private val _seatsState = MutableStateFlow<SeatsUiState>(SeatsUiState.Loading)
    val seatsState: StateFlow<SeatsUiState> = _seatsState.asStateFlow()

    val selectedSeatIds = MutableStateFlow<Set<Long>>(emptySet())
    val totalPrice = MutableStateFlow(0.0)

    private val _reservationState = MutableStateFlow<ReservationUiState>(ReservationUiState.Idle)
    val reservationState: StateFlow<ReservationUiState> = _reservationState.asStateFlow()

    init {
        loadSeats()
    }

    fun loadSeats() {
        viewModelScope.launch {
            _seatsState.value = SeatsUiState.Loading
            try {
                val rows = NetworkModule.screeningApi.getSeats(screeningId)
                _seatsState.value = SeatsUiState.Success(rows)
                selectedSeatIds.value = emptySet()
                totalPrice.value = 0.0
            } catch (e: Exception) {
                _seatsState.value = SeatsUiState.Error(e.message ?: "Błąd pobierania miejsc")
            }
        }
    }

    fun toggleSeat(seat: SeatDto) {
        if (!seat.isAvailable) return
        val current = selectedSeatIds.value.toMutableSet()
        if (current.contains(seat.id)) current.remove(seat.id) else current.add(seat.id)
        selectedSeatIds.value = current
        totalPrice.value = current.size * basePrice
    }

    fun reserve() {
        val ids = selectedSeatIds.value.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            _reservationState.value = ReservationUiState.Loading
            try {
                val request = CreateReservationRequest(
                    screeningId = screeningId,
                    seatIds = ids,
                    ticketTypes = ids.map { "NORMALNY" }
                )
                val response = NetworkModule.reservationApi.createReservation(request)
                when {
                    response.isSuccessful -> {
                        val code = response.body()?.reservationCode ?: "N/A"
                        _reservationState.value = ReservationUiState.Success(code)
                    }
                    response.code() == 409 -> {
                        _reservationState.value = ReservationUiState.Conflict(
                            "Jedno z wybranych miejsc zostało właśnie zajęte. Wybierz inne."
                        )
                    }
                    else -> {
                        _reservationState.value = ReservationUiState.Error("Błąd serwera: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                _reservationState.value = ReservationUiState.Error(e.message ?: "Błąd połączenia")
            }
        }
    }

    fun resetReservationState() {
        _reservationState.value = ReservationUiState.Idle
    }
}

class SeatSelectionViewModelFactory(
    private val screeningId: Long,
    private val basePrice: Double
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        SeatSelectionViewModel(screeningId, basePrice) as T
}
