package com.cinema.booking.service;

import com.cinema.booking.model.Movie;
import com.cinema.booking.repository.MovieRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private MovieService movieService;

    @Test
    void shouldGetAllMovies() {
        Movie movie = Movie.builder().title("Test").build();
        when(movieRepository.findAll()).thenReturn(List.of(movie));

        List<Movie> result = movieService.getAllMovies();

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldAddMovie() {
        Movie movie = Movie.builder().title("New").build();
        when(movieRepository.save(movie)).thenReturn(movie);

        Movie result = movieService.addMovie(movie);

        assertThat(result).isNotNull();
        verify(movieRepository).save(movie);
    }
}