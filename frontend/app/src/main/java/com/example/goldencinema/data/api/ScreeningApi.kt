package com.example.goldencinema

import retrofit2.http.GET
import retrofit2.http.Path

/** Interfejs Retrofit do pobierania danych o seansach i dostępności miejsc. */
interface ScreeningApi {

    /**
     * Zwraca listę nadchodzących seansów.
     *
     * @return lista seansów z danymi filmu i sali
     */
    @GET("screenings")
    suspend fun getScreenings(): List<ScreeningDto>

    /**
     * Zwraca układ miejsc z dostępnością dla danego seansu.
     *
     * @param screeningId identyfikator seansu
     * @return lista rzędów z miejscami i ich statusem (dostępne/zajęte)
     */
    @GET("screenings/{id}/seats")
    suspend fun getSeats(@Path("id") screeningId: Long): List<SeatRowDto>
}
