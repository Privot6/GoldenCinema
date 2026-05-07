package com.goldencinema.backend.dto;

public record SeatAvailabilityDto(
        Long id,
        String rowLabel,
        Integer seatNumber,
        boolean isAvailable
) {
}