package com.cinema.booking.service;

import com.cinema.booking.dto.BookTicketDto;
import com.cinema.booking.dto.SeatDto;
import com.cinema.booking.dto.TicketSelection;
import com.cinema.booking.exception.ResourceNotFoundException;
import com.cinema.booking.model.*;
import com.cinema.booking.repository.ScreeningRepository;
import com.cinema.booking.repository.SeatRepository;
import com.cinema.booking.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final ScreeningRepository screeningRepository;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;

    @Transactional(readOnly = true)
    public List<SeatDto> getSeatsForScreening(Long screeningId) {
        log.debug("Pobieranie miejsc dla seansu ID: {}", screeningId);
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
        log.info("Rozpoczęcie rezerwacji dla seansu: {}, liczba biletów: {}",
                request.screeningId(), request.tickets().size());

        Screening screening = screeningRepository.findById(request.screeningId())
                .orElseThrow(() -> new ResourceNotFoundException("Screening not found"));

        List<Long> seatIds = request.tickets().stream()
                .map(TicketSelection::seatId)
                .toList();

        List<Seat> seats = seatRepository.findAllById(seatIds);

        if (seats.size() != seatIds.size()) {
            throw new IllegalArgumentException("Nieprawidłowe identyfikatory miejsc");
        }

        boolean anyTaken = ticketRepository.findByScreeningId(screening.getId())
                .stream()
                .anyMatch(t -> seatIds.contains(t.getSeat().getId()));

        if (anyTaken) {
            throw new IllegalArgumentException("Jedno z wybranych miejsc jest już zajęte");
        }

        Map<Long, TicketType> seatTypeMap = request.tickets().stream()
                .collect(Collectors.toMap(TicketSelection::seatId, TicketSelection::ticketType));

        List<Long> ticketIds = new ArrayList<>();

        for (Seat seat : seats) {
            TicketType type = seatTypeMap.get(seat.getId());
            BigDecimal price = calculatePrice(type);

            Ticket ticket = Ticket.builder()
                    .screening(screening)
                    .seat(seat)
                    .price(price)
                    .type(type)
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

    private BigDecimal calculatePrice(TicketType type) {
        return switch (type) {
            case NORMAL -> BigDecimal.valueOf(25.00);
            case REDUCED -> BigDecimal.valueOf(18.00);
            case FAMILY -> BigDecimal.valueOf(15.00);
        };
    }
}