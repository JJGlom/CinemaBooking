package com.cinema.booking.repository;

import com.cinema.booking.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TicketRepositoryTest {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Screening screening;
    private Seat seat;

    @BeforeEach
    void setUp() {
        Room room = Room.builder().name("Room 1").capacity(10).build();
        entityManager.persist(room);

        Movie movie = Movie.builder().title("Movie").genre("Action").director("Dir").durationMinutes(120).build();
        entityManager.persist(movie);

        screening = Screening.builder().room(room).movie(movie).startTime(LocalDateTime.now()).build();
        entityManager.persist(screening);

        seat = Seat.builder().room(room).rowNumber(1).seatNumber(1).build();
        entityManager.persist(seat);
    }

    @Test
    void shouldSaveTicket() {
        Ticket ticket = Ticket.builder()
                .screening(screening)
                .seat(seat)
                .price(BigDecimal.TEN)
                .type(TicketType.NORMAL)
                .paid(true)
                .build();

        Ticket savedTicket = ticketRepository.save(ticket);

        assertThat(savedTicket.getId()).isNotNull();
        assertThat(savedTicket.getTicketIdentifier()).isNotNull();
    }

    @Test
    void shouldFindTicketById() {
        Ticket ticket = Ticket.builder().screening(screening).seat(seat).price(BigDecimal.TEN).type(TicketType.NORMAL).paid(true).build();
        Ticket persisted = entityManager.persist(ticket);

        Optional<Ticket> found = ticketRepository.findById(persisted.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(persisted.getId());
    }

    @Test
    void shouldFindTicketsByScreeningId() {
        Ticket ticket1 = Ticket.builder().screening(screening).seat(seat).price(BigDecimal.TEN).type(TicketType.NORMAL).paid(true).build();
        entityManager.persist(ticket1);

        Seat seat2 = Seat.builder().room(seat.getRoom()).rowNumber(1).seatNumber(2).build();
        entityManager.persist(seat2);

        Ticket ticket2 = Ticket.builder().screening(screening).seat(seat2).price(BigDecimal.TEN).type(TicketType.NORMAL).paid(true).build();
        entityManager.persist(ticket2);

        List<Ticket> tickets = ticketRepository.findByScreeningId(screening.getId());

        assertThat(tickets).hasSize(2);
    }

    @Test
    void shouldDeleteTicket() {
        Ticket ticket = Ticket.builder().screening(screening).seat(seat).price(BigDecimal.TEN).type(TicketType.NORMAL).paid(true).build();
        Ticket persisted = entityManager.persist(ticket);

        ticketRepository.deleteById(persisted.getId());

        Optional<Ticket> found = ticketRepository.findById(persisted.getId());
        assertThat(found).isEmpty();
    }
}