package com.goldencinema.backend.dto;

import com.goldencinema.backend.entity.ReservationStatus;

import java.math.BigDecimal;
import java.util.List;

public record ReservationResponse(
        Long id,
        String reservationCode,
        ReservationStatus status,
        BigDecimal totalPrice,
        ScreeningResponse screeningDto,
        List<ReservedSeatDto> reservedSeatsDto
) {
}