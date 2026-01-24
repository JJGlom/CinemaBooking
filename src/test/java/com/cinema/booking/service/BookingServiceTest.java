package com.cinema.booking.service;

import com.cinema.booking.dto.BookTicketDto;
import com.cinema.booking.dto.SeatDto;
import com.cinema.booking.dto.TicketSelection;
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
        TicketSelection ts1 = new TicketSelection(10L, TicketType.NORMAL);
        Screening screening = Screening.builder().id(screeningId).startTime(LocalDateTime.now().plusHours(2)).build();
        Seat seat1 = Seat.builder().id(10L).build();

        when(screeningRepository.findById(screeningId)).thenReturn(Optional.of(screening));
        when(seatRepository.findAllById(anyList())).thenReturn(List.of(seat1));
        when(ticketRepository.findByScreeningId(screeningId)).thenReturn(Collections.emptyList());

        Ticket savedTicket = Ticket.builder().id(100L).price(BigDecimal.valueOf(25)).build();
        when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);

        BookTicketDto request = new BookTicketDto(screeningId, List.of(ts1));
        List<Long> resultIds = bookingService.createReservation(request);

        assertThat(resultIds).hasSize(1);
    }

    @Test
    void shouldCreateReservationWithEmptyTicketList() {
        Long screeningId = 1L;
        Screening screening = Screening.builder().id(screeningId).build();

        when(screeningRepository.findById(screeningId)).thenReturn(Optional.of(screening));
        when(seatRepository.findAllById(Collections.emptyList())).thenReturn(Collections.emptyList());
        when(ticketRepository.findByScreeningId(screeningId)).thenReturn(Collections.emptyList());

        BookTicketDto request = new BookTicketDto(screeningId, Collections.emptyList());
        List<Long> resultIds = bookingService.createReservation(request);

        assertThat(resultIds).isEmpty();
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void shouldCalculatePriceForFamilyTicket() {
        Long screeningId = 1L;
        TicketSelection ts1 = new TicketSelection(12L, TicketType.FAMILY);
        Screening screening = Screening.builder().id(screeningId).startTime(LocalDateTime.now()).build();
        Seat seat = Seat.builder().id(12L).build();

        when(screeningRepository.findById(screeningId)).thenReturn(Optional.of(screening));
        when(seatRepository.findAllById(anyList())).thenReturn(List.of(seat));
        when(ticketRepository.findByScreeningId(screeningId)).thenReturn(Collections.emptyList());
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(i -> {
            Ticket t = i.getArgument(0);
            t.setId(100L);
            return t;
        });

        bookingService.createReservation(new BookTicketDto(screeningId, List.of(ts1)));

        verify(ticketRepository).save(argThat(ticket ->
                ticket.getPrice().compareTo(BigDecimal.valueOf(15.00)) == 0
        ));
    }

    @Test
    void shouldCalculatePriceForReducedTicket() {
        Long screeningId = 1L;
        TicketSelection ts1 = new TicketSelection(13L, TicketType.REDUCED);
        Screening screening = Screening.builder().id(screeningId).startTime(LocalDateTime.now()).build();
        Seat seat = Seat.builder().id(13L).build();

        when(screeningRepository.findById(screeningId)).thenReturn(Optional.of(screening));
        when(seatRepository.findAllById(anyList())).thenReturn(List.of(seat));
        when(ticketRepository.findByScreeningId(screeningId)).thenReturn(Collections.emptyList());
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(i -> {
            Ticket t = i.getArgument(0);
            t.setId(100L);
            return t;
        });

        bookingService.createReservation(new BookTicketDto(screeningId, List.of(ts1)));

        verify(ticketRepository).save(argThat(ticket ->
                ticket.getPrice().compareTo(BigDecimal.valueOf(18.00)) == 0
        ));
    }

    @Test
    void shouldCalculatePriceForNormalTicket() {
        Long screeningId = 1L;
        TicketSelection ts1 = new TicketSelection(14L, TicketType.NORMAL);
        Screening screening = Screening.builder().id(screeningId).startTime(LocalDateTime.now()).build();
        Seat seat = Seat.builder().id(14L).build();

        when(screeningRepository.findById(screeningId)).thenReturn(Optional.of(screening));
        when(seatRepository.findAllById(anyList())).thenReturn(List.of(seat));
        when(ticketRepository.findByScreeningId(screeningId)).thenReturn(Collections.emptyList());
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(i -> {
            Ticket t = i.getArgument(0);
            t.setId(100L);
            return t;
        });

        bookingService.createReservation(new BookTicketDto(screeningId, List.of(ts1)));

        verify(ticketRepository).save(argThat(ticket ->
                ticket.getPrice().compareTo(BigDecimal.valueOf(25.00)) == 0
        ));
    }

    @Test
    void shouldCreateReservationWhenOtherTicketsExistButNoConflict() {
        Long screeningId = 1L;
        TicketSelection ts1 = new TicketSelection(10L, TicketType.NORMAL);
        Screening screening = Screening.builder().id(screeningId).startTime(LocalDateTime.now()).build();
        Seat seat1 = Seat.builder().id(10L).build();

        Seat occupiedSeat = Seat.builder().id(99L).build();
        Ticket existingTicket = Ticket.builder().seat(occupiedSeat).build();

        when(screeningRepository.findById(screeningId)).thenReturn(Optional.of(screening));
        when(seatRepository.findAllById(anyList())).thenReturn(List.of(seat1));
        when(ticketRepository.findByScreeningId(screeningId)).thenReturn(List.of(existingTicket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(Ticket.builder().id(100L).build());

        BookTicketDto request = new BookTicketDto(screeningId, List.of(ts1));
        List<Long> resultIds = bookingService.createReservation(request);

        assertThat(resultIds).hasSize(1);
    }

    @Test
    void shouldThrowExceptionWhenSeatIsAlreadyTaken() {
        Long screeningId = 1L;
        TicketSelection ts1 = new TicketSelection(10L, TicketType.NORMAL);
        Screening screening = Screening.builder().id(screeningId).build();
        Seat seat = Seat.builder().id(10L).build();
        Ticket existingTicket = Ticket.builder().seat(seat).build();

        when(screeningRepository.findById(screeningId)).thenReturn(Optional.of(screening));
        when(seatRepository.findAllById(anyList())).thenReturn(List.of(seat));
        when(ticketRepository.findByScreeningId(screeningId)).thenReturn(List.of(existingTicket));

        BookTicketDto request = new BookTicketDto(screeningId, List.of(ts1));

        assertThatThrownBy(() -> bookingService.createReservation(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowNotFoundWhenScreeningDoesNotExistDuringReservation() {
        BookTicketDto request = new BookTicketDto(999L, Collections.emptyList());
        when(screeningRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createReservation(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldReturnSeatsWithAvailabilityStatus() {
        Long screeningId = 1L;
        Long roomId = 5L;
        Room room = Room.builder().id(roomId).build();
        Screening screening = Screening.builder().id(screeningId).room(room).build();
        Seat seat1 = Seat.builder().id(10L).room(room).build();
        Seat seat2 = Seat.builder().id(11L).room(room).build();
        Ticket ticketForSeat1 = Ticket.builder().seat(seat1).build();

        when(screeningRepository.findById(screeningId)).thenReturn(Optional.of(screening));
        when(seatRepository.findByRoomId(roomId)).thenReturn(List.of(seat1, seat2));
        when(ticketRepository.findByScreeningId(screeningId)).thenReturn(List.of(ticketForSeat1));

        List<SeatDto> result = bookingService.getSeatsForScreening(screeningId);

        assertThat(result).hasSize(2);
        assertThat(result.stream().anyMatch(s -> s.id().equals(10L) && !s.available())).isTrue();
        assertThat(result.stream().anyMatch(s -> s.id().equals(11L) && s.available())).isTrue();
    }

    @Test
    void shouldThrowNotFoundWhenScreeningDoesNotExistForSeats() {
        when(screeningRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> bookingService.getSeatsForScreening(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldConfirmPayment() {
        List<Long> ids = List.of(1L);
        Ticket ticket = Ticket.builder().id(1L).paid(false).build();
        when(ticketRepository.findAllById(ids)).thenReturn(List.of(ticket));
        bookingService.confirmPayment(ids);
        assertThat(ticket.isPaid()).isTrue();
    }

    @Test
    void shouldHandleNullOrEmptyCancelList() {
        bookingService.cancelReservation(null);
        bookingService.cancelReservation(Collections.emptyList());
        verify(ticketRepository, never()).deleteAll(any());
    }

    @Test
    void shouldCancelReservation() {
        List<Long> ids = List.of(1L);
        Ticket ticket = Ticket.builder().id(1L).build();
        when(ticketRepository.findAllById(ids)).thenReturn(List.of(ticket));
        bookingService.cancelReservation(ids);
        verify(ticketRepository).deleteAll(List.of(ticket));
    }

    @Test
    void shouldGetTicketsByIds() {
        List<Long> ids = List.of(1L);
        Ticket ticket = Ticket.builder().id(1L).build();
        when(ticketRepository.findAllById(ids)).thenReturn(List.of(ticket));
        List<Ticket> result = bookingService.getTicketsByIds(ids);
        assertThat(result).hasSize(1);
    }
}