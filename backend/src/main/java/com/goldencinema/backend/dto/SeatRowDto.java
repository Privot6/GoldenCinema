package com.goldencinema.backend.dto;

import java.util.List;

public record SeatRowDto(
        String rowLabel,
        List<SeatAvailabilityDto> seats
) {
}