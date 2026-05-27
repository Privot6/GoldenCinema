package com.example.goldencinema

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface EmployeeApi {

    @GET("employee/reservations/verify/{code}")
    suspend fun verifyReservation(
        @Path("code") code: String
    ): Response<ReservationVerificationDto>

    @PATCH("employee/reservations/{id}/status")
    suspend fun updateReservationStatus(
        @Path("id") id: Long,
        @Body request: UpdateStatusRequest
    ): Response<Unit>
}
