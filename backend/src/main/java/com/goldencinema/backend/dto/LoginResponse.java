package com.goldencinema.backend.dto;

public record LoginResponse(
        String token,
        String type
) {
}