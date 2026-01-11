package com.cinema.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record CreateScreeningDto(
        @NotNull(message = "Movie ID is required")
        Long movieId,

        @NotNull(message = "Room ID is required")
        Long roomId,

        @NotNull(message = "Start time is required")
        @Future(message = "Screening must be in the future")
        LocalDateTime startTime
) {}