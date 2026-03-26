package com.goldencinema.backend.dto;

public record RepertuarItemDto(
        Long id,
        String title,
        String date,
        String time,
        String hall
) {
}