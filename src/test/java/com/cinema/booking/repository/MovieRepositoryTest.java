package com.cinema.booking.repository;

import com.cinema.booking.model.Movie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MovieRepositoryTest {

    @Autowired
    private MovieRepository movieRepository;

    @Test
    void shouldSaveAndFindMovie() {
        Movie movie = Movie.builder()
                .title("Inception")
                .genre("Sci-Fi")
                .director("Christopher Nolan")
                .durationMinutes(148)
                .build();

        Movie savedMovie = movieRepository.save(movie);

        Optional<Movie> foundMovie = movieRepository.findById(savedMovie.getId());

        assertThat(foundMovie).isPresent();
        assertThat(foundMovie.get().getTitle()).isEqualTo("Inception");
        assertThat(foundMovie.get().getId()).isNotNull();
    }
}