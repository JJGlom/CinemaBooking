package com.cinema.booking.dto;

import com.cinema.booking.model.MovieImage;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import java.util.List;

@Builder
public record MovieDto(
        Long id,

        @NotBlank(message = "Tytuł jest wymagany")
        String title,

        @Size(max = 2000, message = "Opis nie może być dłuższy niż 2000 znaków")
        String description,

        @NotBlank(message = "Gatunek jest wymagany")
        String genre,

        @NotBlank(message = "Reżyser jest wymagany")
        String director,

        @Min(value = 0, message = "Czas trwania musi być dodatni")
        Integer durationMinutes,

        @Min(value = 0, message = "Ograniczenie wiekowe musi być liczbą dodatnią lub 0")
        Integer ageRestriction,

        String posterUrl,
        String trailerUrl,
        String castMembers,
        List<MovieImage> images
) {}