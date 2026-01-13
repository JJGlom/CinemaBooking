package com.cinema.booking.service;

import com.cinema.booking.dto.BookTicketDto;
import com.cinema.booking.exception.ResourceNotFoundException;
import com.cinema.booking.model.*;
import com.cinema.booking.repository.ScreeningRepository;
import com.cinema.booking.repository.SeatRepository;
import com.cinema.booking.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private ScreeningRepository screeningRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void shouldBookTicketsSuccessfully() {
        Long screeningId = 1L;
        List<Long> seatIds = List.of(10L, 11L);

        Screening screening = Screening.builder()
                .id(screeningId)
                .startTime(LocalDateTime.now().plusHours(2))
                .build();

        Seat seat1 = Seat.builder().id(10L).rowNumber(1).seatNumber(1).build();
        Seat seat2 = Seat.builder().id(11L).rowNumber(1).seatNumber(2).build();

        when(screeningRepository.findById(screeningId)).thenReturn(Optional.of(screening));
        when(seatRepository.findAllById(seatIds)).thenReturn(List.of(seat1, seat2));
        when(ticketRepository.findByScreeningId(screeningId)).thenReturn(Collections.emptyList());

        Ticket savedTicket = Ticket.builder().id(100L).price(BigDecimal.valueOf(25)).build();
        when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);

        BookTicketDto request = new BookTicketDto(screeningId, seatIds);

        List<Long> resultIds = bookingService.bookTicket(request);

        assertThat(resultIds).hasSize(2);
        verify(ticketRepository, times(2)).save(any(Ticket.class));
    }

    @Test
    void shouldThrowExceptionWhenSeatIsAlreadyTaken() {
        Long screeningId = 1L;
        List<Long> seatIds = List.of(10L);

        Screening screening = Screening.builder().id(screeningId).build();
        Seat seat = Seat.builder().id(10L).build();

        Ticket existingTicket = Ticket.builder()
                .id(99L)
                .seat(seat)
                .screening(screening)
                .build();

        when(screeningRepository.findById(screeningId)).thenReturn(Optional.of(screening));
        when(seatRepository.findAllById(seatIds)).thenReturn(List.of(seat));
        when(ticketRepository.findByScreeningId(screeningId)).thenReturn(List.of(existingTicket));

        BookTicketDto request = new BookTicketDto(screeningId, seatIds);

        assertThatThrownBy(() -> bookingService.bookTicket(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("zajęte");

        verify(ticketRepository, never()).save(any(Ticket.class));
    }
}