package com.goldencinema.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Imię jest wymagane")
        @Size(max = 100, message = "Imię może mieć maksymalnie 100 znaków")
        String firstName,

        @NotBlank(message = "Nazwisko jest wymagane")
        @Size(max = 100, message = "Nazwisko może mieć maksymalnie 100 znaków")
        String lastName,

        @NotBlank(message = "Email jest wymagany")
        @Email(message = "Nieprawidłowy format email")
        @Size(max = 150, message = "Email może mieć maksymalnie 150 znaków")
        String email,

        @Size(max = 20, message = "Telefon może mieć maksymalnie 20 znaków")
        String phone,

        @NotBlank(message = "Hasło jest wymagane")
        @Size(min = 6, max = 255, message = "Hasło musi mieć od 6 do 255 znaków")
        String password
) {
}