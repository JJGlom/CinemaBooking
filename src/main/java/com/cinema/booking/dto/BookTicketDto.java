package com.cinema.booking.dto;

import jakarta.validation.constraints.NotNull;

public record BookTicketDto(
        @NotNull(message = "Screening ID is required")
        Long screeningId,

        @NotNull(message = "Seat ID is required")
        Long seatId
) {}