package com.goldencinema.backend.dto;

import com.goldencinema.backend.entity.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminReservationDto(
        Long id,
        String reservationCode,
        ReservationStatus status,
        BigDecimal totalPrice,
        LocalDateTime createdAt,
        String userFirstName,
        String userLastName,
        String userEmail,
        String movieTitle,
        String hallName,
        LocalDateTime screeningStartTime
) {}
