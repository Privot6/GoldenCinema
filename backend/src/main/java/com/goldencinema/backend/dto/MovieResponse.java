package com.goldencinema.backend.dto;

import java.time.LocalDateTime;

public record MovieResponse(
        Long id,
        String title,
        String description,
        Integer durationMinutes,
        String ageRating,
        String language,
        String subtitles,
        String genre,
        String posterUrl,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}