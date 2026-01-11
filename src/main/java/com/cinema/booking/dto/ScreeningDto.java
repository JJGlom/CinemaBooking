package com.cinema.booking.dto;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record ScreeningDto(
        Long id,
        Long movieId,
        String movieTitle,
        Long roomId,
        String roomName,
        LocalDateTime startTime,
        LocalDateTime endTime
) {}