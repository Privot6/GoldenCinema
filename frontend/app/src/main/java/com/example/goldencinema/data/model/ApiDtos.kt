package com.example.goldencinema

import com.google.gson.annotations.SerializedName

// ── Auth ──────────────────────────────────────────────────────────────────────
data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val token: String, val type: String)
data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val phone: String? = null
)
data class UserProfileDto(val email: String?, val firstName: String?, val lastName: String?, val phone: String?)

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

// Response from POST /api/reservations — contains Stripe checkout URL
data class CreateReservationResponseDto(
    @SerializedName("reservationId") val id: Long,
    val reservationCode: String,
    val status: String,
    val totalPrice: Double,
    val paymentUrl: String,
    val sessionId: String
)

// Response from GET /api/reservations/my
data class ReservationResponseDto(
    val id: Long,
    val reservationCode: String,
    val status: String,
    val totalPrice: Double,
    val screeningDto: ScreeningDto,
    val reservedSeatsDto: List<ReservedSeatDto>
)

// Response from GET /api/reservations/{id}/checkout
data class CheckoutUrlDto(val paymentUrl: String)

data class ReservedSeatDto(
    val seatId: Long,
    val rowLabel: String,
    val seatNumber: Int,
    val ticketType: String,
    val price: Double
)

// ── Employee / Scanner ────────────────────────────────────────────────────────
data class ReservationVerificationDto(
    val id: Long,
    val reservationCode: String,
    val status: String,
    val isValid: Boolean,
    val invalidReason: String?,
    val userFirstName: String,
    val userLastName: String,
    val movieTitle: String,
    val hallName: String,
    val screeningStartTime: String,
    val totalPrice: Double,
    val seatCount: Int
)

data class UpdateStatusRequest(val status: String)
