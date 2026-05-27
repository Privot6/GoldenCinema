package com.goldencinema.backend.dto;

import com.goldencinema.backend.entity.ReservationStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReservationVerificationDto(
        Long id,
        String reservationCode,
        ReservationStatus status,
        boolean isValid,
        String invalidReason,
        String userFirstName,
        String userLastName,
        String movieTitle,
        String hallName,
        LocalDateTime screeningStartTime,
        BigDecimal totalPrice,
        int seatCount
) {}
