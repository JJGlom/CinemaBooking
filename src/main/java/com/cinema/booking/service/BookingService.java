package com.cinema.booking.service;

import com.cinema.booking.dto.BookTicketDto;
import com.cinema.booking.dto.SeatDto;
import com.cinema.booking.exception.ResourceNotFoundException;
import com.cinema.booking.model.*;
import com.cinema.booking.repository.ScreeningRepository;
import com.cinema.booking.repository.SeatRepository;
import com.cinema.booking.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final ScreeningRepository screeningRepository;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;

    @Transactional(readOnly = true)
    public List<SeatDto> getSeatsForScreening(Long screeningId) {
        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new ResourceNotFoundException("Screening not found"));

        List<Seat> allSeats = seatRepository.findByRoomId(screening.getRoom().getId());
        List<Ticket> soldTickets = ticketRepository.findByScreeningId(screeningId);

        Set<Long> reservedSeatIds = soldTickets.stream()
                .map(ticket -> ticket.getSeat().getId())
                .collect(Collectors.toSet());

        return allSeats.stream()
                .map(seat -> SeatDto.builder()
                        .id(seat.getId())
                        .rowNumber(seat.getRowNumber())
                        .seatNumber(seat.getSeatNumber())
                        .available(!reservedSeatIds.contains(seat.getId()))
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public Long bookTicket(BookTicketDto request) {
        Screening screening = screeningRepository.findById(request.screeningId())
                .orElseThrow(() -> new ResourceNotFoundException("Screening not found"));

        Seat seat = seatRepository.findById(request.seatId())
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found"));

        boolean isSeatTaken = ticketRepository.findByScreeningId(screening.getId())
                .stream()
                .anyMatch(t -> t.getSeat().getId().equals(seat.getId()));

        if (isSeatTaken) {
            throw new IllegalArgumentException("Seat is already reserved");
        }

        Ticket ticket = Ticket.builder()
                .screening(screening)
                .seat(seat)
                .price(BigDecimal.valueOf(25.00))
                .type(TicketType.NORMAL)
                .ticketIdentifier(UUID.randomUUID().toString())
                .paid(true)
                .build();

        return ticketRepository.save(ticket).getId();
    }
}