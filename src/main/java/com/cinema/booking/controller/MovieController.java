package com.cinema.booking.controller;

import com.cinema.booking.dto.MovieDto;
import com.cinema.booking.model.Movie;
import com.cinema.booking.service.MovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @GetMapping
    public ResponseEntity<Page<MovieDto>> getAllMovies(Pageable pageable) {
        Page<Movie> movies = movieService.getAllMovies(pageable);
        Page<MovieDto> dtos = movies.map(this::mapToDto);
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
        Movie savedMovie = movieService.addMovie(movie, null);
        return new ResponseEntity<>(mapToDto(savedMovie), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MovieDto> updateMovie(@PathVariable Long id, @Valid @RequestBody MovieDto movieDto) {
        Movie movie = mapToEntity(movieDto);
        Movie updatedMovie = movieService.updateMovie(id, movie, null);
        return ResponseEntity.ok(mapToDto(updatedMovie));
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