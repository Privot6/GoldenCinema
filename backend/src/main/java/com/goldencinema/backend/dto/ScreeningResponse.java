package com.goldencinema.backend.dto;

import com.goldencinema.backend.entity.ScreeningStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ScreeningResponse(
        Long id,
        LocalDateTime startTime,
        LocalDateTime endTime,
        BigDecimal basePrice,
        ScreeningStatus status,
        MovieDto movie,
        HallDto hall
) {}