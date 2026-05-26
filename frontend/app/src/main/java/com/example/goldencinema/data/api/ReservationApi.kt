package com.example.goldencinema

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ReservationApi {
    @POST("reservations")
    suspend fun createReservation(@Body request: CreateReservationRequest): Response<ReservationResponseDto>

    @GET("reservations/my")
    suspend fun getMyReservations(): List<ReservationResponseDto>
}
