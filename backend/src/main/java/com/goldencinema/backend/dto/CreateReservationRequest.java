package com.goldencinema.backend.dto;

import com.goldencinema.backend.entity.TicketType;

import java.util.List;

public record CreateReservationRequest(
        Long screeningId,
        List<Long> seatIds,
        List<TicketType> ticketTypes
) {
}