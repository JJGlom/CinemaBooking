package com.cinema.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record CreateScreeningDto(
        @NotNull(message = "Musisz wybrać film")
        Long movieId,

        @NotNull(message = "Musisz wybrać salę")
        Long roomId,

        @NotNull(message = "Data i godzina rozpoczęcia są wymagane")
        @Future(message = "Seans musi odbyć się w przyszłości")
        LocalDateTime startTime
) {}