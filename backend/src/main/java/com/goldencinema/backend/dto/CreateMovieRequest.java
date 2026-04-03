package com.goldencinema.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateMovieRequest(
        @NotBlank(message = "Tytuł jest wymagany")
        String title,

        @Size(max = 2000, message = "Opis może mieć maksymalnie 2000 znaków")
        String description,

        @NotNull(message = "Czas trwania jest wymagany")
        @Min(value = 1, message = "Czas trwania musi być większy od 0")
        Integer durationMinutes,

        @NotBlank(message = "Gatunek jest wymagany")
        String genre,

        @NotBlank(message = "Ograniczenie wiekowe jest wymagane")
        String ageRating
) {
}