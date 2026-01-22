package com.cinema.booking.controller;

import com.cinema.booking.dto.BookTicketDto;
import com.cinema.booking.dto.SeatDto;
import com.cinema.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Booking Controller", description = "Zarządzanie rezerwacjami i miejscami")
public class BookingController {

    private final BookingService bookingService;

    @Operation(summary = "Pobierz miejsca dla seansu", description = "Zwraca listę wszystkich miejsc z informacją o dostępności")
    @GetMapping("/screening/{screeningId}/seats")
    public ResponseEntity<List<SeatDto>> getSeatsForScreening(@PathVariable Long screeningId) {
        return ResponseEntity.ok(bookingService.getSeatsForScreening(screeningId));
    }

    @Operation(summary = "Utwórz rezerwację", description = "Blokuje wybrane miejsca i tworzy tymczasowe bilety")
    @PostMapping("/reserve")
    public ResponseEntity<Void> reserveTickets(@Valid @RequestBody BookTicketDto bookTicketDto, HttpSession session) {
        List<Long> ticketIds = bookingService.createReservation(bookTicketDto);
        session.setAttribute("cartTickets", ticketIds);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(summary = "Opłać bilety", description = "Zatwierdza rezerwację zmieniając status biletów na opłacone")
    @PostMapping("/pay")
    public ResponseEntity<Void> payForTickets(@RequestBody List<Long> ticketIds, HttpSession session) {
        bookingService.confirmPayment(ticketIds);
        session.removeAttribute("cartTickets");
        return ResponseEntity.ok().build();
    }
}