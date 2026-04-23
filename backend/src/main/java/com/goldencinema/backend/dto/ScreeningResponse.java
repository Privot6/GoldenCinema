package com.goldencinema.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ScreeningResponse(
        Long id,
        LocalDateTime startTime,
        LocalDateTime endTime,
        BigDecimal basePrice,
        MovieDto movie,
        HallDto hall
) {}