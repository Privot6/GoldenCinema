package com.goldencinema.backend.dto;

public record MovieDto(
        Long id,
        String title,
        Integer durationMinutes,
        String genre,
        String posterUrl
) {
}