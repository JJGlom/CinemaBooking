package com.cinema.booking.controller;

import com.cinema.booking.dto.BookTicketDto;
import com.cinema.booking.dto.SeatDto;
import com.cinema.booking.service.BookingService;
import jakarta.servlet.http.HttpSession;
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

    @PostMapping("/reserve")
    public ResponseEntity<Void> reserveTickets(@Valid @RequestBody BookTicketDto bookTicketDto, HttpSession session) {
        List<Long> ticketIds = bookingService.createReservation(bookTicketDto);
        session.setAttribute("cartTickets", ticketIds);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/pay")
    public ResponseEntity<Void> payForTickets(@RequestBody List<Long> ticketIds, HttpSession session) {
        bookingService.confirmPayment(ticketIds);
        session.removeAttribute("cartTickets");
        return ResponseEntity.ok().build();
    }
}