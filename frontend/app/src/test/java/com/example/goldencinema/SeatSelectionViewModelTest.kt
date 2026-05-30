package com.example.goldencinema

import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import kotlin.test.assertIs
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SeatSelectionViewModelTest : BaseViewModelTest() {

    private val mockScreeningApi = mockk<ScreeningApi>()
    private val mockReservationApi = mockk<ReservationApi>()

    @Before
    fun setUp() {
        mockkObject(NetworkModule)
        io.mockk.every { NetworkModule.screeningApi } returns mockScreeningApi
        io.mockk.every { NetworkModule.reservationApi } returns mockReservationApi
    }

    private fun fakeSeat(id: Long, available: Boolean = true) = SeatDto(
        id = id,
        rowLabel = "A",
        seatNumber = id.toInt(),
        isAvailable = available,
        gridCol = id.toInt() - 1
    )

    private fun fakeRow(vararg seats: SeatDto) = SeatRowDto("A", seats.toList())

    @Test
    fun `init pobiera miejsca i emituje Success`() = runTest(testDispatcher) {
        coEvery { mockScreeningApi.getSeats(1L) } returns listOf(fakeRow(fakeSeat(1), fakeSeat(2)))

        val viewModel = SeatSelectionViewModel(screeningId = 1L, basePrice = 25.0)

        val state = viewModel.seatsState.value
        assertIs<SeatsUiState.Success>(state)
        assertEquals(1, state.rows.size)
    }

    @Test
    fun `init blad sieci emituje Error`() = runTest(testDispatcher) {
        coEvery { mockScreeningApi.getSeats(1L) } throws java.io.IOException("Brak sieci")

        val viewModel = SeatSelectionViewModel(screeningId = 1L, basePrice = 25.0)

        assertIs<SeatsUiState.Error>(viewModel.seatsState.value)
    }

    @Test
    fun `toggleSeat dostepne miejsce dodaje do selectedSeatIds`() = runTest(testDispatcher) {
        coEvery { mockScreeningApi.getSeats(1L) } returns listOf(fakeRow(fakeSeat(1)))
        val viewModel = SeatSelectionViewModel(screeningId = 1L, basePrice = 25.0)

        viewModel.toggleSeat(fakeSeat(1))

        assertTrue(viewModel.selectedSeatIds.value.contains(1L))
    }

    @Test
    fun `toggleSeat ponownie usuwa zaznaczenie`() = runTest(testDispatcher) {
        coEvery { mockScreeningApi.getSeats(1L) } returns listOf(fakeRow(fakeSeat(1)))
        val viewModel = SeatSelectionViewModel(screeningId = 1L, basePrice = 25.0)

        viewModel.toggleSeat(fakeSeat(1))
        viewModel.toggleSeat(fakeSeat(1))

        assertTrue(viewModel.selectedSeatIds.value.isEmpty())
    }

    @Test
    fun `toggleSeat niedostepne miejsce ignoruje`() = runTest(testDispatcher) {
        coEvery { mockScreeningApi.getSeats(1L) } returns listOf(fakeRow(fakeSeat(1, available = false)))
        val viewModel = SeatSelectionViewModel(screeningId = 1L, basePrice = 25.0)

        viewModel.toggleSeat(fakeSeat(1, available = false))

        assertTrue(viewModel.selectedSeatIds.value.isEmpty())
    }

    @Test
    fun `toggleSeat aktualizuje laczna cene`() = runTest(testDispatcher) {
        coEvery { mockScreeningApi.getSeats(1L) } returns listOf(fakeRow(fakeSeat(1), fakeSeat(2)))
        val viewModel = SeatSelectionViewModel(screeningId = 1L, basePrice = 25.0)

        viewModel.toggleSeat(fakeSeat(1))
        assertEquals(25.0, viewModel.totalPrice.value)

        viewModel.toggleSeat(fakeSeat(2))
        assertEquals(50.0, viewModel.totalPrice.value)
    }

    @Test
    fun `reserve sukces emituje ReadyForPayment`() = runTest(testDispatcher) {
        coEvery { mockScreeningApi.getSeats(1L) } returns listOf(fakeRow(fakeSeat(1)))
        coEvery { mockReservationApi.createReservation(any()) } returns Response.success(
            CreateReservationResponseDto(
                id = 1L,
                reservationCode = "ABC12345",
                status = "OCZEKUJACA",
                totalPrice = 25.0,
                paymentUrl = "https://stripe.com/pay",
                sessionId = "sess_123"
            )
        )
        val viewModel = SeatSelectionViewModel(screeningId = 1L, basePrice = 25.0)
        viewModel.toggleSeat(fakeSeat(1))

        viewModel.reserve()

        val state = viewModel.reservationState.value
        assertIs<ReservationUiState.ReadyForPayment>(state)
        assertEquals("ABC12345", state.reservationCode)
    }

    @Test
    fun `reserve konflikt 409 emituje Conflict`() = runTest(testDispatcher) {
        coEvery { mockScreeningApi.getSeats(1L) } returns listOf(fakeRow(fakeSeat(1)))
        coEvery { mockReservationApi.createReservation(any()) } returns
                Response.error(409, "".toResponseBody())
        val viewModel = SeatSelectionViewModel(screeningId = 1L, basePrice = 25.0)
        viewModel.toggleSeat(fakeSeat(1))

        viewModel.reserve()

        assertIs<ReservationUiState.Conflict>(viewModel.reservationState.value)
    }

    @Test
    fun `reserve pusta lista nie zmienia stanu`() = runTest(testDispatcher) {
        coEvery { mockScreeningApi.getSeats(1L) } returns listOf(fakeRow(fakeSeat(1)))
        val viewModel = SeatSelectionViewModel(screeningId = 1L, basePrice = 25.0)

        viewModel.reserve()

        assertIs<ReservationUiState.Idle>(viewModel.reservationState.value)
    }

    @Test
    fun `resetReservationState zmienia stan na Idle`() = runTest(testDispatcher) {
        coEvery { mockScreeningApi.getSeats(1L) } returns listOf(fakeRow(fakeSeat(1)))
        coEvery { mockReservationApi.createReservation(any()) } returns Response.success(
            CreateReservationResponseDto(1L, "ABC", "OK", 25.0, "https://stripe.com", "sess")
        )
        val viewModel = SeatSelectionViewModel(screeningId = 1L, basePrice = 25.0)
        viewModel.toggleSeat(fakeSeat(1))
        viewModel.reserve()
        assertIs<ReservationUiState.ReadyForPayment>(viewModel.reservationState.value)

        viewModel.resetReservationState()

        assertIs<ReservationUiState.Idle>(viewModel.reservationState.value)
    }
}
