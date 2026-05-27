package com.example.goldencinema

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ScanUiState {
    object Scanning : ScanUiState()
    object Loading : ScanUiState()
    data class Found(val dto: ReservationVerificationDto) : ScanUiState()
    data class Error(val message: String) : ScanUiState()
}

class QrScannerViewModel : ViewModel() {

    private val _state = MutableStateFlow<ScanUiState>(ScanUiState.Scanning)
    val state: StateFlow<ScanUiState> = _state.asStateFlow()

    @Volatile private var isProcessing = false

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
