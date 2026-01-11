package com.cinema.booking.controller;

import com.cinema.booking.dto.MovieDto;
import com.cinema.booking.model.Movie;
import com.cinema.booking.service.MovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @GetMapping
    public ResponseEntity<List<MovieDto>> getAllMovies() {
        List<Movie> movies = movieService.getAllMovies();
        List<MovieDto> dtos = movies.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieDto> getMovieById(@PathVariable Long id) {
        Movie movie = movieService.getMovieById(id);
        return ResponseEntity.ok(mapToDto(movie));
    }

    @PostMapping
    public ResponseEntity<MovieDto> createMovie(@Valid @RequestBody MovieDto movieDto) {
        Movie movie = mapToEntity(movieDto);
        Movie savedMovie = movieService.addMovie(movie);
        return new ResponseEntity<>(mapToDto(savedMovie), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }

    private MovieDto mapToDto(Movie movie) {
        return MovieDto.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .description(movie.getDescription())
                .genre(movie.getGenre())
                .director(movie.getDirector())
                .durationMinutes(movie.getDurationMinutes())
                .ageRestriction(movie.getAgeRestriction())
                .posterUrl(movie.getPosterUrl())
                .trailerUrl(movie.getTrailerUrl())
                .castMembers(movie.getCastMembers())
                .build();
    }

    private Movie mapToEntity(MovieDto dto) {
        return Movie.builder()
                .id(dto.id())
                .title(dto.title())
                .description(dto.description())
                .genre(dto.genre())
                .director(dto.director())
                .durationMinutes(dto.durationMinutes())
                .ageRestriction(dto.ageRestriction())
                .posterUrl(dto.posterUrl())
                .trailerUrl(dto.trailerUrl())
                .castMembers(dto.castMembers())
                .build();
    }
}