package com.cinema.booking.repository;

import com.cinema.booking.model.Movie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
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
    }

    @Test
    void shouldFindMoviesByGenre() {
        Movie m1 = Movie.builder().title("A").genre("Comedy").director("X").durationMinutes(90).build();
        Movie m2 = Movie.builder().title("B").genre("Comedy").director("Y").durationMinutes(100).build();

        movieRepository.save(m1);
        movieRepository.save(m2);

        List<Movie> comedies = movieRepository.findByGenre("Comedy");

        assertThat(comedies).hasSize(2);
    }

    @Test
    void shouldDeleteMovie() {
        Movie movie = Movie.builder().title("To Delete").genre("Drama").director("Z").durationMinutes(120).build();
        Movie saved = movieRepository.save(movie);

        movieRepository.deleteById(saved.getId());

        Optional<Movie> found = movieRepository.findById(saved.getId());
        assertThat(found).isEmpty();
    }
}