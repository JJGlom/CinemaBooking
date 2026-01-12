package com.cinema.booking.dto;

import lombok.Builder;

@Builder
public record SeatDto(
        Long id,
        int rowNumber,
        int seatNumber,
        boolean available
) {}