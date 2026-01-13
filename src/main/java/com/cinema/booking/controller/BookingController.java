package com.cinema.booking.controller;

import com.cinema.booking.dto.BookTicketDto;
import com.cinema.booking.dto.SeatDto;
import com.cinema.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping("/screening/{screeningId}/seats")
    public ResponseEntity<List<SeatDto>> getSeatsForScreening(@PathVariable Long screeningId) {
        return ResponseEntity.ok(bookingService.getSeatsForScreening(screeningId));
    }

    @PostMapping
    public ResponseEntity<List<Long>> bookTicket(@Valid @RequestBody BookTicketDto bookTicketDto) {
        List<Long> ticketIds = bookingService.bookTicket(bookTicketDto);
        return new ResponseEntity<>(ticketIds, HttpStatus.CREATED);
    }
}