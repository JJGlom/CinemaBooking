package com.cinema.booking.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        int status,
        String message,
        List<String> details,
        LocalDateTime timestamp
) {}