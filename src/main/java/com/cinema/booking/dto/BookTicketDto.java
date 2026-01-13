package com.cinema.booking.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record BookTicketDto(
        @NotNull(message = "Screening ID is required")
        Long screeningId,

        @NotEmpty(message = "Musisz wybrać przynajmniej jedno miejsce")
        List<Long> seatIds
) {}