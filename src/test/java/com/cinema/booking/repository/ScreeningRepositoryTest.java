package com.cinema.booking.repository;

import com.cinema.booking.model.Movie;
import com.cinema.booking.model.Room;
import com.cinema.booking.model.Screening;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ScreeningRepositoryTest {

    @Autowired
    private ScreeningRepository screeningRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Room room;
    private Movie movie;

    @BeforeEach
    void setUp() {
        room = Room.builder().name("Room A").capacity(50).build();
        entityManager.persist(room);

        movie = Movie.builder().title("Matrix").genre("Sci-Fi").director("Wachowski").durationMinutes(136).build();
        entityManager.persist(movie);
    }

    @Test
    void shouldFindScreeningsByTimeRange() {
        LocalDateTime now = LocalDateTime.now();
        Screening s1 = Screening.builder().movie(movie).room(room).startTime(now.plusHours(1)).build();
        Screening s2 = Screening.builder().movie(movie).room(room).startTime(now.plusHours(5)).build();

        entityManager.persist(s1);
        entityManager.persist(s2);

        List<Screening> result = screeningRepository.findByStartTimeBetween(now, now.plusHours(2));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStartTime()).isEqualTo(s1.getStartTime());
    }

    @Test
    void shouldFindScreeningsByRoomAndTimeOverlap() {
        LocalDateTime start = LocalDateTime.now().plusHours(2);
        Screening s1 = Screening.builder().movie(movie).room(room).startTime(start).build();
        entityManager.persist(s1);

        List<Screening> result = screeningRepository.findByRoomIdAndStartTimeBetween(
                room.getId(), start.minusHours(1), start.plusHours(1));

        assertThat(result).isNotEmpty();
    }

    @Test
    void shouldFindScreeningsByMovieId() {
        Screening s1 = Screening.builder().movie(movie).room(room).startTime(LocalDateTime.now().plusHours(1)).build();
        entityManager.persist(s1);

        List<Screening> result = screeningRepository.findByMovieId(movie.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMovie().getTitle()).isEqualTo("Matrix");
    }
}