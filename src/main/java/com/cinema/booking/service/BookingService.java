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
import java.util.ArrayList;
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
    public List<Long> bookTicket(BookTicketDto request) {
        Screening screening = screeningRepository.findById(request.screeningId())
                .orElseThrow(() -> new ResourceNotFoundException("Screening not found"));

        List<Seat> seats = seatRepository.findAllById(request.seatIds());

        if (seats.size() != request.seatIds().size()) {
            throw new IllegalArgumentException("Nieprawidłowe identyfikatory miejsc");
        }

        boolean anyTaken = ticketRepository.findByScreeningId(screening.getId())
                .stream()
                .anyMatch(t -> request.seatIds().contains(t.getSeat().getId()));

        if (anyTaken) {
            throw new IllegalArgumentException("Jedno z wybranych miejsc jest już zajęte");
        }

        List<Long> ticketIds = new ArrayList<>();

        for (Seat seat : seats) {
            Ticket ticket = Ticket.builder()
                    .screening(screening)
                    .seat(seat)
                    .price(BigDecimal.valueOf(25.00))
                    .type(TicketType.NORMAL)
                    .ticketIdentifier(UUID.randomUUID().toString())
                    .paid(true)
                    .build();

            ticketIds.add(ticketRepository.save(ticket).getId());
        }

        return ticketIds;
    }

    @Transactional(readOnly = true)
    public Ticket getTicketById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
    }

    @Transactional(readOnly = true)
    public List<Ticket> getTicketsByIds(List<Long> ids) {
        return ticketRepository.findAllById(ids);
    }
}