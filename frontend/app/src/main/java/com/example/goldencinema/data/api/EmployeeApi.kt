package com.example.goldencinema

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

/** Interfejs Retrofit do operacji pracownika kina — weryfikacja biletów i zmiana statusów. */
interface EmployeeApi {

    /**
     * Weryfikuje rezerwację na podstawie kodu z kodu QR.
     *
     * @param code unikalny kod rezerwacji
     * @return wynik weryfikacji lub błąd HTTP
     */
    @GET("employee/reservations/verify/{code}")
    suspend fun verifyReservation(
        @Path("code") code: String
    ): Response<ReservationVerificationDto>

    /**
     * Zmienia status rezerwacji (np. OCZEKUJACA → POTWIERDZONA po wejściu).
     *
     * @param id      identyfikator rezerwacji
     * @param request nowy status rezerwacji
     * @return 200 OK lub błąd HTTP
     */
    @PATCH("employee/reservations/{id}/status")
    suspend fun updateReservationStatus(
        @Path("id") id: Long,
        @Body request: UpdateStatusRequest
    ): Response<Unit>
}
