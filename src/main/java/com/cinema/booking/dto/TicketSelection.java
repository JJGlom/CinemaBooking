package com.cinema.booking.dto;

import com.cinema.booking.model.TicketType;
import jakarta.validation.constraints.NotNull;

public record TicketSelection(
        @NotNull Long seatId,
        @NotNull TicketType ticketType
) {}