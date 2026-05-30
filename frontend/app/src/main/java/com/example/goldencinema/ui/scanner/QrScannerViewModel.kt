package com.example.goldencinema

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Stan skanera kodów QR pracownika. */
sealed class ScanUiState {
    /** Kamera aktywna — oczekiwanie na zeskanowanie kodu. */
    object Scanning : ScanUiState()
    /** Trwa weryfikacja kodu w API. */
    object Loading : ScanUiState()
    /** Rezerwacja znaleziona — wyświetlane szczegóły do potwierdzenia wejścia. */
    data class Found(val dto: ReservationVerificationDto) : ScanUiState()
    /** Błąd weryfikacji lub sieciowy — automatycznie resetuje się po 3 sekundach. */
    data class Error(val message: String) : ScanUiState()
}

/**
 * ViewModel skanera QR dla pracownika kina.
 * Obsługuje skanowanie kodów QR z biletów, weryfikację rezerwacji i potwierdzenie wejścia.
 *
 * @property state aktualny stan skanera
 */
class QrScannerViewModel : ViewModel() {

    private val _state = MutableStateFlow<ScanUiState>(ScanUiState.Scanning)
    val state: StateFlow<ScanUiState> = _state.asStateFlow()

    @Volatile private var isProcessing = false

    /**
     * Obsługuje zeskanowany tekst z kodu QR. Ignoruje duplikaty skanów.
     * Parsuje kod rezerwacji i weryfikuje go w API.
     *
     * @param rawText surowy tekst ze zeskanowanego kodu QR
     */
    fun onQrCodeScanned(rawText: String) {
        if (isProcessing || _state.value !is ScanUiState.Scanning) return
        isProcessing = true
        val code = parseCode(rawText)
        viewModelScope.launch {
            _state.value = ScanUiState.Loading
            try {
                val resp = NetworkModule.employeeApi.verifyReservation(code)
                when {
                    resp.isSuccessful && resp.body() != null ->
                        _state.value = ScanUiState.Found(resp.body()!!)
                    resp.code() == 404 -> {
                        _state.value = ScanUiState.Error("Nie znaleziono rezerwacji: $code")
                        resetAfterDelay()
                    }
                    else -> {
                        _state.value = ScanUiState.Error("Błąd serwera: ${resp.code()}")
                        resetAfterDelay()
                    }
                }
            } catch (e: Exception) {
                _state.value = ScanUiState.Error("Błąd połączenia: ${e.message}")
                resetAfterDelay()
            } finally {
                isProcessing = false
            }
        }
    }

    /**
     * Potwierdza wejście — zmienia status rezerwacji na POTWIERDZONA i wraca do skanowania po 2 sekundach.
     *
     * @param reservationId identyfikator rezerwacji do potwierdzenia
     */
    fun confirmEntry(reservationId: Long) {
        viewModelScope.launch {
            try {
                NetworkModule.employeeApi.updateReservationStatus(
                    reservationId,
                    UpdateStatusRequest("POTWIERDZONA")
                )
            } catch (_: Exception) {}
            delay(2_000L)
            resetToScanning()
        }
    }

    /** Resetuje skaner do stanu [ScanUiState.Scanning] — gotowy na kolejne skanowanie. */
    fun resetToScanning() {
        _state.value = ScanUiState.Scanning
        isProcessing = false
    }

    private fun resetAfterDelay() {
        viewModelScope.launch {
            delay(3_000L)
            resetToScanning()
        }
    }

    private fun parseCode(raw: String): String =
        raw.lines().firstOrNull { it.startsWith("Kod: ") }
            ?.removePrefix("Kod: ")?.trim()
            ?: raw.trim()
}
