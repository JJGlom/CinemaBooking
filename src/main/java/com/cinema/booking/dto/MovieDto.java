package com.cinema.booking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record MovieDto(
        Long id,

        @NotBlank(message = "Tytuł jest wymagany")
        String title,

        String description,

        @NotBlank(message = "Gatunek jest wymagany")
        String genre,

        @NotBlank(message = "Reżyser jest wymagany")
        String director,

        @Min(value = 0, message = "Czas trwania musi być dodatni")
        Integer durationMinutes,

        Integer ageRestriction,

        String posterUrl,    // Tu był błąd (średnik zamiast przecinka)
        String trailerUrl,   // Tu też
        String castMembers
) {}