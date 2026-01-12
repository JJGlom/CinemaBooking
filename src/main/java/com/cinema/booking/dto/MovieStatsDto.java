package com.cinema.booking.dto;

import java.math.BigDecimal;

public record MovieStatsDto(
        String movieTitle,
        Long ticketsSold,
        BigDecimal totalRevenue
) {}