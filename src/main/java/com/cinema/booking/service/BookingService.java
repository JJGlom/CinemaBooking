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
import java.time.LocalDateTime;
import java.util.*;
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
    public List<Long> createReservation(BookTicketDto request) {
        Screening screening = screeningRepository.findById(request.screeningId())
                .orElseThrow(() -> new ResourceNotFoundException("Screening not found"));

        List<Long> seatIds = request.tickets().stream().map(TicketSelection::seatId).toList();
        List<Seat> seats = seatRepository.findAllById(seatIds);

        boolean anyTaken = ticketRepository.findByScreeningId(screening.getId()).stream()
                .anyMatch(t -> seatIds.contains(t.getSeat().getId()));

        if (anyTaken) throw new IllegalArgumentException("Miejsca zajęte");

        Map<Long, TicketType> seatTypeMap = request.tickets().stream()
                .collect(Collectors.toMap(TicketSelection::seatId, TicketSelection::ticketType));

        List<Long> ticketIds = new ArrayList<>();
        for (Seat seat : seats) {
            TicketType type = seatTypeMap.get(seat.getId());

            Ticket ticket = Ticket.builder()
                    .screening(screening)
                    .seat(seat)
                    .type(type)
                    .price(calculatePrice(type))
                    .ticketIdentifier(UUID.randomUUID().toString())
                    .paid(false)
                    .purchaseDate(LocalDateTime.now())
                    .build();

            ticketIds.add(ticketRepository.save(ticket).getId());
        }
        return ticketIds;
    }

    @Transactional
    public void confirmPayment(List<Long> ticketIds) {
        List<Ticket> tickets = ticketRepository.findAllById(ticketIds);
        tickets.forEach(t -> t.setPaid(true));
        ticketRepository.saveAll(tickets);
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