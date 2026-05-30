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
class AuthViewModelTest : BaseViewModelTest() {

    private val mockAuthApi = mockk<AuthApi>()
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        mockkObject(NetworkModule)
        mockkObject(TokenStore)
        io.mockk.every { NetworkModule.authApi } returns mockAuthApi
        justRun { TokenStore.save(any()) }
        viewModel = AuthViewModel()
    }

    @Test
    fun `login sukces - stan to Success`() = runTest(testDispatcher) {
        coEvery { mockAuthApi.login(any()) } returns
                Response.success(LoginResponse("token_jwt", "Bearer"))

        viewModel.login("user@test.com", "Test1234!")

        assertIs<LoginUiState.Success>(viewModel.loginState.value)
    }

    @Test
    fun `login zle haslo 401 - stan to Error z komunikatem`() = runTest(testDispatcher) {
        coEvery { mockAuthApi.login(any()) } returns
                Response.error(401, "".toResponseBody())

        viewModel.login("user@test.com", "ZleHaslo!")

        val state = viewModel.loginState.value
        assertIs<LoginUiState.Error>(state)
        assertTrue(state.message.contains("Nieprawidłowy"))
    }

    @Test
    fun `login blad serwera 500 - stan to Error`() = runTest(testDispatcher) {
        coEvery { mockAuthApi.login(any()) } returns
                Response.error(500, "".toResponseBody())

        viewModel.login("user@test.com", "Test1234!")

        val state = viewModel.loginState.value
        assertIs<LoginUiState.Error>(state)
        assertTrue(state.message.contains("500"))
    }

    @Test
    fun `login wyjątek sieciowy - stan to Error z połączeniem`() = runTest(testDispatcher) {
        coEvery { mockAuthApi.login(any()) } throws java.io.IOException("Brak sieci")

        viewModel.login("user@test.com", "Test1234!")

        val state = viewModel.loginState.value
        assertIs<LoginUiState.Error>(state)
        assertTrue(state.message.contains("połączenia"))
    }

    @Test
    fun `resetState zmienia stan na Idle`() = runTest(testDispatcher) {
        coEvery { mockAuthApi.login(any()) } returns
                Response.success(LoginResponse("token_jwt", "Bearer"))
        viewModel.login("user@test.com", "Test1234!")
        assertIs<LoginUiState.Success>(viewModel.loginState.value)

        viewModel.resetState()

        assertIs<LoginUiState.Idle>(viewModel.loginState.value)
    }

    @Test
    fun `stan poczatkowy to Idle`() = runTest(testDispatcher) {
        assertIs<LoginUiState.Idle>(viewModel.loginState.value)
    }
}
