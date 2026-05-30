package com.example.goldencinema

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/** Interfejs Retrofit do zarządzania rezerwacjami biletów. */
interface ReservationApi {

    /**
     * Tworzy rezerwację i inicjuje sesję płatności Stripe.
     *
     * @param request dane rezerwacji (seans, miejsca, typy biletów)
     * @return rezerwacja z kodem i URL do płatności lub błąd HTTP
     */
    @POST("reservations")
    suspend fun createReservation(@Body request: CreateReservationRequest): Response<CreateReservationResponseDto>

    /**
     * Zwraca listę rezerwacji zalogowanego użytkownika.
     *
     * @return lista rezerwacji bieżącego użytkownika
     */
    @GET("reservations/my")
    suspend fun getMyReservations(): List<ReservationResponseDto>

    /**
     * Generuje nowy URL do płatności dla istniejącej, nieopłaconej rezerwacji.
     *
     * @param reservationId identyfikator rezerwacji
     * @return URL do sesji płatności Stripe lub błąd HTTP
     */
    @GET("reservations/{id}/checkout")
    suspend fun getCheckoutUrl(@Path("id") reservationId: Long): Response<CheckoutUrlDto>
}
