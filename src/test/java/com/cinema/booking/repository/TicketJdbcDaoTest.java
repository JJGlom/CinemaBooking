package com.cinema.booking.repository;

import com.cinema.booking.model.Movie;
import com.cinema.booking.model.Room;
import com.cinema.booking.model.Screening;
import com.cinema.booking.model.Seat;
import com.cinema.booking.model.Ticket;
import com.cinema.booking.model.TicketType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TicketJdbcDao.class)
class TicketJdbcDaoTest {

    @Autowired
    private TicketJdbcDao ticketJdbcDao;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ScreeningRepository screeningRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long ticketId;

    @BeforeEach
    void setUp() {
        Room room = roomRepository.save(Room.builder().name("Test Room").capacity(10).build());
        Movie movie = movieRepository.save(Movie.builder().title("Test Movie").genre("Action").director("X").durationMinutes(120).build());
        Screening screening = screeningRepository.save(Screening.builder().room(room).movie(movie).startTime(LocalDateTime.now().plusHours(2)).build());
        Seat seat = seatRepository.save(Seat.builder().room(room).rowNumber(1).seatNumber(1).build());

        Ticket ticket = Ticket.builder()
                .screening(screening)
                .seat(seat)
                .price(BigDecimal.TEN)
                .type(TicketType.NORMAL)
                .paid(false)
                .purchaseDate(LocalDateTime.now().minusMinutes(30))
                .build();

        ticketId = ticketRepository.save(ticket).getId();
    }

    @Test
    void shouldMarkTicketAsPaid() {
        ticketJdbcDao.markTicketAsPaid(ticketId);

        Boolean isPaid = jdbcTemplate.queryForObject(
                "SELECT paid FROM tickets WHERE id = ?", Boolean.class, ticketId);

        assertThat(isPaid).isTrue();
    }

    @Test
    void shouldDeleteOldUnpaidTickets() {
        ticketJdbcDao.deleteUnpaidTicketsOlderThan(15);

        Long count = ticketRepository.count();
        assertThat(count).isEqualTo(0);
    }
}