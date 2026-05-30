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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ReservationsViewModelTest : BaseViewModelTest() {

    private val mockReservationApi = mockk<ReservationApi>()

    @Before
    fun setUp() {
        mockkObject(NetworkModule)
        io.mockk.every { NetworkModule.reservationApi } returns mockReservationApi
    }

    private fun fakeReservation() = ReservationResponseDto(
        id = 1L,
        reservationCode = "ABC12345",
        status = "OCZEKUJACA",
        totalPrice = 25.0,
        screeningDto = ScreeningDto(
            id = 1L, startTime = "2026-06-01T18:00:00", endTime = "2026-06-01T20:00:00",
            basePrice = 25.0, status = "ZAPLANOWANY",
            movie = MovieDto(1L, "Test Film", 120, "Akcja", null),
            hall = HallDto(1L, "Sala Główna")
        ),
        reservedSeatsDto = emptyList()
    )

    @Test
    fun `init pobiera rezerwacje i emituje Success`() = runTest(testDispatcher) {
        coEvery { mockReservationApi.getMyReservations() } returns listOf(fakeReservation())

        val viewModel = ReservationsViewModel()

        assertIs<ReservationsUiState.Success>(viewModel.state.value)
    }

    @Test
    fun `init blad sieci emituje Error`() = runTest(testDispatcher) {
        coEvery { mockReservationApi.getMyReservations() } throws java.io.IOException("Brak sieci")

        val viewModel = ReservationsViewModel()

        assertIs<ReservationsUiState.Error>(viewModel.state.value)
    }

    @Test
    fun `startPayment sukces emituje OpenUrl`() = runTest(testDispatcher) {
        coEvery { mockReservationApi.getMyReservations() } returns emptyList()
        coEvery { mockReservationApi.getCheckoutUrl(1L) } returns
                Response.success(CheckoutUrlDto("https://checkout.stripe.com/test"))

        val viewModel = ReservationsViewModel()
        viewModel.startPayment(1L)

        val event = viewModel.checkoutEvent.value
        assertIs<CheckoutEvent.OpenUrl>(event)
        assertTrue(event.url.contains("stripe"))
    }

    @Test
    fun `startPayment blad serwera emituje Error`() = runTest(testDispatcher) {
        coEvery { mockReservationApi.getMyReservations() } returns emptyList()
        coEvery { mockReservationApi.getCheckoutUrl(1L) } returns
                Response.error(500, "".toResponseBody())

        val viewModel = ReservationsViewModel()
        viewModel.startPayment(1L)

        assertIs<CheckoutEvent.Error>(viewModel.checkoutEvent.value)
    }

    @Test
    fun `startPayment wyjątek sieciowy emituje Error`() = runTest(testDispatcher) {
        coEvery { mockReservationApi.getMyReservations() } returns emptyList()
        coEvery { mockReservationApi.getCheckoutUrl(any()) } throws java.io.IOException("Brak sieci")

        val viewModel = ReservationsViewModel()
        viewModel.startPayment(1L)

        assertIs<CheckoutEvent.Error>(viewModel.checkoutEvent.value)
    }

    @Test
    fun `resetCheckoutEvent zmienia stan na Idle`() = runTest(testDispatcher) {
        coEvery { mockReservationApi.getMyReservations() } returns emptyList()
        coEvery { mockReservationApi.getCheckoutUrl(any()) } returns
                Response.success(CheckoutUrlDto("https://stripe.com"))

        val viewModel = ReservationsViewModel()
        viewModel.startPayment(1L)
        assertIs<CheckoutEvent.OpenUrl>(viewModel.checkoutEvent.value)

        viewModel.resetCheckoutEvent()

        assertIs<CheckoutEvent.Idle>(viewModel.checkoutEvent.value)
    }

    @Test
    fun `stan poczatkowy checkoutEvent to Idle`() = runTest(testDispatcher) {
        coEvery { mockReservationApi.getMyReservations() } returns emptyList()
        val viewModel = ReservationsViewModel()
        assertIs<CheckoutEvent.Idle>(viewModel.checkoutEvent.value)
    }

    @Test
    fun `refresh po zakonczeniu isRefreshing jest false`() = runTest(testDispatcher) {
        coEvery { mockReservationApi.getMyReservations() } returns listOf(fakeReservation())

        val viewModel = ReservationsViewModel()
        viewModel.refresh()

        assertFalse(viewModel.isRefreshing.value)
    }
}
