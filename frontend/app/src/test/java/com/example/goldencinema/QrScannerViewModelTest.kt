package com.example.goldencinema

import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class QrScannerViewModelTest : BaseViewModelTest() {

    private val mockEmployeeApi = mockk<EmployeeApi>()
    private lateinit var viewModel: QrScannerViewModel

    @Before
    fun setUp() {
        mockkObject(NetworkModule)
        io.mockk.every { NetworkModule.employeeApi } returns mockEmployeeApi
        viewModel = QrScannerViewModel()
    }

    private fun fakeDto(code: String, isValid: Boolean = true) = ReservationVerificationDto(
        id = 1L,
        reservationCode = code,
        status = if (isValid) "OCZEKUJACA" else "ANULOWANA",
        isValid = isValid,
        invalidReason = if (isValid) null else "Rezerwacja jest anulowana",
        userFirstName = "Jan",
        userLastName = "Kowalski",
        movieTitle = "Test Film",
        hallName = "Sala Główna",
        screeningStartTime = "2026-06-01T18:00:00",
        totalPrice = 25.0,
        seatCount = 1
    )

    @Test
    fun `stan poczatkowy to Scanning`() = runTest(testDispatcher) {
        assertIs<ScanUiState.Scanning>(viewModel.state.value)
    }

    @Test
    fun `onQrCodeScanned prawidlowy kod - stan to Found`() = runTest(testDispatcher) {
        coEvery { mockEmployeeApi.verifyReservation("ABC12345") } returns
                Response.success(fakeDto("ABC12345"))

        viewModel.onQrCodeScanned("ABC12345")

        val state = viewModel.state.value
        assertIs<ScanUiState.Found>(state)
        assert(state.dto.reservationCode == "ABC12345")
    }

    @Test
    fun `onQrCodeScanned nieznany kod 404 - stan to Error`() = runTest(testDispatcher) {
        coEvery { mockEmployeeApi.verifyReservation(any()) } returns
                Response.error(404, "".toResponseBody())

        viewModel.onQrCodeScanned("NIEZNANY")

        // Error jest emitowany, ale resetAfterDelay(3s) nie odpala się jeszcze (virtual time)
        assertIs<ScanUiState.Error>(viewModel.state.value)
    }

    @Test
    fun `onQrCodeScanned blad sieci - stan to Error`() = runTest(testDispatcher) {
        coEvery { mockEmployeeApi.verifyReservation(any()) } throws java.io.IOException("Brak sieci")

        viewModel.onQrCodeScanned("DOWOLNY")

        assertIs<ScanUiState.Error>(viewModel.state.value)
    }

    @Test
    fun `onQrCodeScanned parsuje kod z formatu QR biletu`() = runTest(testDispatcher) {
        coEvery { mockEmployeeApi.verifyReservation("ABC12345") } returns
                Response.success(fakeDto("ABC12345"))

        viewModel.onQrCodeScanned("GoldenCinema\nKod: ABC12345\nSeans: Test")

        assertIs<ScanUiState.Found>(viewModel.state.value)
    }

    @Test
    fun `po bledzie resetAfterDelay wraca do Scanning po 3 sekundach`() = runTest(testDispatcher) {
        coEvery { mockEmployeeApi.verifyReservation(any()) } returns
                Response.error(404, "".toResponseBody())

        viewModel.onQrCodeScanned("NIEZNANY")
        assertIs<ScanUiState.Error>(viewModel.state.value)

        // Przesuwamy wirtualny czas o 3s+
        advanceTimeBy(3001)

        assertIs<ScanUiState.Scanning>(viewModel.state.value)
    }

    @Test
    fun `confirmEntry zmienia status i wraca do Scanning`() = runTest(testDispatcher) {
        coEvery { mockEmployeeApi.verifyReservation("ABC12345") } returns
                Response.success(fakeDto("ABC12345"))
        coEvery { mockEmployeeApi.updateReservationStatus(1L, any()) } returns
                Response.success(Unit)

        viewModel.onQrCodeScanned("ABC12345")
        assertIs<ScanUiState.Found>(viewModel.state.value)

        viewModel.confirmEntry(1L)
        advanceTimeBy(2001)

        assertIs<ScanUiState.Scanning>(viewModel.state.value)
    }

    @Test
    fun `resetToScanning zmienia stan na Scanning`() = runTest(testDispatcher) {
        coEvery { mockEmployeeApi.verifyReservation(any()) } returns
                Response.success(fakeDto("ABC12345"))
        viewModel.onQrCodeScanned("ABC12345")
        assertIs<ScanUiState.Found>(viewModel.state.value)

        viewModel.resetToScanning()

        assertIs<ScanUiState.Scanning>(viewModel.state.value)
    }
}
