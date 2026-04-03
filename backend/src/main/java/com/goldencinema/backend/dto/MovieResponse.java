package com.goldencinema.backend.dto;

public record MovieResponse(
        Long id,
        String title,
        String description,
        Integer durationMinutes,
        String genre,
        String ageRating
) {
}