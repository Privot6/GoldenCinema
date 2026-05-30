package com.example.goldencinema

import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertIs
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ScreeningViewModelTest : BaseViewModelTest() {

    private val mockScreeningApi = mockk<ScreeningApi>()

    @Before
    fun setUp() {
        mockkObject(NetworkModule)
        io.mockk.every { NetworkModule.screeningApi } returns mockScreeningApi
    }

    private fun fakeScreening() = ScreeningDto(
        id = 1L,
        startTime = "2026-06-01T18:00:00",
        endTime = "2026-06-01T20:00:00",
        basePrice = 25.0,
        status = "ZAPLANOWANY",
        movie = MovieDto(1L, "Test Film", 120, "Akcja", null),
        hall = HallDto(1L, "Sala Główna")
    )

    @Test
    fun `init pobiera seanse i emituje Success`() = runTest(testDispatcher) {
        val seanse = listOf(fakeScreening())
        coEvery { mockScreeningApi.getScreenings() } returns seanse

        val viewModel = ScreeningViewModel()

        val state = viewModel.state.value
        assertIs<ScreeningsUiState.Success>(state)
        assertEquals(1, state.screenings.size)
    }

    @Test
    fun `init blad sieci emituje Error`() = runTest(testDispatcher) {
        coEvery { mockScreeningApi.getScreenings() } throws java.io.IOException("Brak sieci")

        val viewModel = ScreeningViewModel()

        assertIs<ScreeningsUiState.Error>(viewModel.state.value)
    }

    @Test
    fun `loadScreenings po bledzie odswiezaDane na Success`() = runTest(testDispatcher) {
        coEvery { mockScreeningApi.getScreenings() } throws java.io.IOException("Brak sieci")
        val viewModel = ScreeningViewModel()
        assertIs<ScreeningsUiState.Error>(viewModel.state.value)

        coEvery { mockScreeningApi.getScreenings() } returns listOf(fakeScreening())
        viewModel.loadScreenings()

        assertIs<ScreeningsUiState.Success>(viewModel.state.value)
    }

    @Test
    fun `loadScreenings zwraca pusta liste gdy brak seansow`() = runTest(testDispatcher) {
        coEvery { mockScreeningApi.getScreenings() } returns emptyList()

        val viewModel = ScreeningViewModel()

        val state = viewModel.state.value
        assertIs<ScreeningsUiState.Success>(state)
        assertEquals(0, state.screenings.size)
    }
}
