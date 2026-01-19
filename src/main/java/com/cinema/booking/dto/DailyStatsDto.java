package com.cinema.booking.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyStatsDto(
        LocalDate date,
        Long ticketsSold,
        BigDecimal totalRevenue
) {}