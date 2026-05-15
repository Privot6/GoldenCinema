package com.goldencinema.backend.dto;

import com.goldencinema.backend.entity.TicketType;

import java.math.BigDecimal;

public record ReservedSeatDto(
        Long seatId,
        String rowLabel,
        Integer seatNumber,
        TicketType ticketType,
        BigDecimal price
) {
}