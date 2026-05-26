package com.example.goldencinema

import retrofit2.http.GET
import retrofit2.http.Path

interface ScreeningApi {
    @GET("screenings")
    suspend fun getScreenings(): List<ScreeningDto>

    @GET("screenings/{id}/seats")
    suspend fun getSeats(@Path("id") screeningId: Long): List<SeatRowDto>
}
