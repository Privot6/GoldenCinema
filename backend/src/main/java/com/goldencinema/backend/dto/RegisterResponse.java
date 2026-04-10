package com.goldencinema.backend.dto;

public record RegisterResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        Boolean isActive
) {
}