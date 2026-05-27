package com.goldencinema.backend.dto;

import com.goldencinema.backend.entity.ReservationStatus;

import java.math.BigDecimal;

public record ReservationPaymentResponse(
        Long reservationId,
        String reservationCode,
        ReservationStatus status,
        BigDecimal totalPrice,
        String paymentUrl,
        String sessionId
) {}