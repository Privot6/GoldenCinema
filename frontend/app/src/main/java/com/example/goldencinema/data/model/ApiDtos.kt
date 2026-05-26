package com.example.goldencinema

import com.google.gson.annotations.SerializedName

// ── Auth ──────────────────────────────────────────────────────────────────────
data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val token: String, val type: String)
data class UserProfileDto(val email: String?, val firstName: String?, val lastName: String?)

// ── Screenings ─────────────────────────────────────────────────────────────────
data class ScreeningDto(
    val id: Long,
    val startTime: String,   // "2026-05-26T14:00:00" — ISO from Spring Boot
    val endTime: String,
    val basePrice: Double,
    val status: String,
    val movie: MovieDto,
    val hall: HallDto
)

data class MovieDto(
    val id: Long,
    val title: String,
    val durationMinutes: Int,
    val genre: String,
    val posterUrl: String?
)

data class HallDto(val id: Long, val name: String)

// ── Seats ─────────────────────────────────────────────────────────────────────
data class SeatRowDto(
    val rowLabel: String,
    val seats: List<SeatDto>
)

data class SeatDto(
    val id: Long,
    val rowLabel: String,
    val seatNumber: Int,
    @SerializedName("isAvailable")
    val isAvailable: Boolean,
    val gridCol: Int?
)

// ── Reservations ──────────────────────────────────────────────────────────────
data class CreateReservationRequest(
    val screeningId: Long,
    val seatIds: List<Long>,
    val ticketTypes: List<String>   // "NORMALNY" | "ULGOWY"
)

data class ReservationResponseDto(
    val id: Long,
    val reservationCode: String,
    val status: String,
    val totalPrice: Double,
    val screeningDto: ScreeningDto,
    val reservedSeatsDto: List<ReservedSeatDto>
)

data class ReservedSeatDto(
    val seatId: Long,
    val rowLabel: String,
    val seatNumber: Int,
    val ticketType: String,
    val price: Double
)
