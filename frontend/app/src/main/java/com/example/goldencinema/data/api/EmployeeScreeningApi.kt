package com.example.goldencinema

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

/** Interfejs Retrofit do zarządzania seansami przez pracownika. */
interface EmployeeScreeningApi {

    /**
     * Pobiera dane seansu po identyfikatorze.
     *
     * @param id identyfikator seansu
     * @return dane seansu
     */
    @GET("employee/screenings/{id}")
    suspend fun getScreening(@Path("id") id: Long): ScreeningDto

    /**
     * Aktualizuje dane seansu.
     *
     * @param id      identyfikator seansu
     * @param request nowe dane seansu
     * @return zaktualizowany seans
     */
    @PUT("employee/screenings/{id}")
    suspend fun updateScreening(
        @Path("id") id: Long,
        @Body request: UpdateScreeningRequest
    ): ScreeningDto
}
