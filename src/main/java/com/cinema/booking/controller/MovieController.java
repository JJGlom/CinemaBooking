package com.cinema.booking.controller;

import com.cinema.booking.dto.MovieDto;
import com.cinema.booking.model.Actor;
import com.cinema.booking.model.Movie;
import com.cinema.booking.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/movies")
@RequiredArgsConstructor
@Tag(name = "Movie Controller", description = "Operacje CRUD na filmach")
public class MovieController {

    private final MovieService movieService;

    @Operation(summary = "Pobierz listę filmów", description = "Zwraca listę filmów z paginacją")
    @GetMapping
    public ResponseEntity<Page<MovieDto>> getAllMovies(Pageable pageable) {
        Page<Movie> movies = movieService.getAllMovies(pageable);
        Page<MovieDto> dtos = movies.map(this::mapToDto);
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Pobierz szczegóły filmu", description = "Zwraca pełne dane filmu na podstawie ID")
    @GetMapping("/{id}")
    public ResponseEntity<MovieDto> getMovieById(@PathVariable Long id) {
        Movie movie = movieService.getMovieById(id);
        return ResponseEntity.ok(mapToDto(movie));
    }

    @Operation(summary = "Dodaj nowy film", description = "Tworzy nowy film w bazie danych")
    @PostMapping
    public ResponseEntity<MovieDto> createMovie(@Valid @RequestBody MovieDto movieDto) {
        Movie movie = mapToEntity(movieDto);
        Movie savedMovie = movieService.addMovie(movie, movieDto.castMembers(), null);
        return new ResponseEntity<>(mapToDto(savedMovie), HttpStatus.CREATED);
    }

    @Operation(summary = "Aktualizuj film", description = "Edytuje dane istniejącego filmu")
    @PutMapping("/{id}")
    public ResponseEntity<MovieDto> updateMovie(@PathVariable Long id, @Valid @RequestBody MovieDto movieDto) {
        Movie movie = mapToEntity(movieDto);
        Movie updatedMovie = movieService.updateMovie(id, movie, movieDto.castMembers(), null, null);
        return ResponseEntity.ok(mapToDto(updatedMovie));
    }

    @Operation(summary = "Usuń film", description = "Usuwa film z bazy danych")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }

    private MovieDto mapToDto(Movie movie) {
        String castMembers = movie.getActors().stream()
                .map(Actor::getName)
                .collect(Collectors.joining(", "));

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
                .castMembers(castMembers)
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
                .build();
    }
}