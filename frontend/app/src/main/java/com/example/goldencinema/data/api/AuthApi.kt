package com.example.goldencinema

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/** Interfejs Retrofit do komunikacji z endpointami uwierzytelniania. */
interface AuthApi {

    /**
     * Loguje użytkownika i zwraca token JWT.
     *
     * @param request dane logowania (email, hasło)
     * @return odpowiedź z tokenem lub błąd HTTP
     */
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    /**
     * Rejestruje nowego użytkownika i zwraca token JWT.
     *
     * @param request dane rejestracyjne (imię, nazwisko, email, hasło)
     * @return odpowiedź z tokenem lub błąd HTTP
     */
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse>

    /**
     * Zwraca profil aktualnie zalogowanego użytkownika na podstawie tokenu JWT.
     *
     * @return dane profilu użytkownika
     */
    @GET("auth/me")
    suspend fun getProfile(): UserProfileDto
}
