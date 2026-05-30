package com.example.goldencinema

import io.mockk.coEvery
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest : BaseViewModelTest() {

    private val mockAuthApi = mockk<AuthApi>()
    private lateinit var viewModel: RegisterViewModel

    @Before
    fun setUp() {
        mockkObject(NetworkModule)
        mockkObject(TokenStore)
        io.mockk.every { NetworkModule.authApi } returns mockAuthApi
        justRun { TokenStore.save(any()) }
        viewModel = RegisterViewModel()
    }

    @Test
    fun `register sukces - stan to Success`() = runTest(testDispatcher) {
        coEvery { mockAuthApi.register(any()) } returns
                Response.success(LoginResponse("token_jwt", "Bearer"))

        viewModel.register("Jan", "Kowalski", "jan@test.com", "Test1234!")

        assertIs<RegisterUiState.Success>(viewModel.state.value)
    }

    @Test
    fun `register email zajety 409 - stan to Error z komunikatem`() = runTest(testDispatcher) {
        coEvery { mockAuthApi.register(any()) } returns
                Response.error(409, "".toResponseBody())

        viewModel.register("Jan", "Kowalski", "zajety@test.com", "Test1234!")

        val state = viewModel.state.value
        assertIs<RegisterUiState.Error>(state)
        assertTrue(state.message.contains("zajęty") || state.message.contains("409"))
    }

    @Test
    fun `register blad serwera 500 - stan to Error`() = runTest(testDispatcher) {
        coEvery { mockAuthApi.register(any()) } returns
                Response.error(500, "".toResponseBody())

        viewModel.register("Jan", "Kowalski", "jan@test.com", "Test1234!")

        val state = viewModel.state.value
        assertIs<RegisterUiState.Error>(state)
        assertTrue(state.message.contains("500"))
    }

    @Test
    fun `register wyjątek sieciowy - stan to Error`() = runTest(testDispatcher) {
        coEvery { mockAuthApi.register(any()) } throws java.io.IOException("Brak sieci")

        viewModel.register("Jan", "Kowalski", "jan@test.com", "Test1234!")

        assertIs<RegisterUiState.Error>(viewModel.state.value)
    }

    @Test
    fun `resetState zmienia stan na Idle`() = runTest(testDispatcher) {
        coEvery { mockAuthApi.register(any()) } returns
                Response.success(LoginResponse("token_jwt", "Bearer"))
        viewModel.register("Jan", "Kowalski", "jan@test.com", "Test1234!")

        viewModel.resetState()

        assertIs<RegisterUiState.Idle>(viewModel.state.value)
    }

    @Test
    fun `stan poczatkowy to Idle`() = runTest(testDispatcher) {
        assertIs<RegisterUiState.Idle>(viewModel.state.value)
    }
}
