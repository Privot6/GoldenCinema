package com.goldencinema.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateMovieRequest(
        @NotBlank(message = "Tytuł jest wymagany")
        @Size(max = 200, message = "Tytuł może mieć maksymalnie 200 znaków")
        String title,

        String description,

        @NotNull(message = "Czas trwania jest wymagany")
        @Min(value = 1, message = "Czas trwania musi być większy od 0")
        Integer durationMinutes,

        @NotBlank(message = "Kategoria wiekowa jest wymagana")
        @Size(max = 20, message = "Kategoria wiekowa może mieć maksymalnie 20 znaków")
        String ageRating,

        @NotBlank(message = "Język jest wymagany")
        @Size(max = 50, message = "Język może mieć maksymalnie 50 znaków")
        String language,

        @Size(max = 50, message = "Napisy mogą mieć maksymalnie 50 znaków")
        String subtitles,

        @NotBlank(message = "Gatunek jest wymagany")
        @Size(max = 100, message = "Gatunek może mieć maksymalnie 100 znaków")
        String genre,

        @Size(max = 255, message = "Poster URL może mieć maksymalnie 255 znaków")
        String posterUrl
) {
}