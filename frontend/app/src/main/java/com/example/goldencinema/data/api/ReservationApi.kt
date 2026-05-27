package com.example.goldencinema

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ReservationApi {
    @POST("reservations")
    suspend fun createReservation(@Body request: CreateReservationRequest): Response<CreateReservationResponseDto>

    @GET("reservations/my")
    suspend fun getMyReservations(): List<ReservationResponseDto>

    @GET("reservations/{id}/checkout")
    suspend fun getCheckoutUrl(@Path("id") reservationId: Long): Response<CheckoutUrlDto>
}
