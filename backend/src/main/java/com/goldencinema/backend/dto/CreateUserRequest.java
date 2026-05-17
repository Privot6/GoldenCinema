package com.goldencinema.backend.dto;

public record CreateUserRequest(
        String firstName,
        String lastName,
        String email,
        String phone,
        String password,
        String role
) {}
