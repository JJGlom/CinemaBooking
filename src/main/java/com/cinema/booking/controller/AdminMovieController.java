package com.cinema.booking.controller;

import com.cinema.booking.dto.MovieDto;
import com.cinema.booking.model.Movie;
import com.cinema.booking.service.MovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Controller
@RequestMapping("/admin/movies")
@RequiredArgsConstructor
public class AdminMovieController {

    private final MovieService movieService;

    @GetMapping
    public String listMovies(Model model) {
        model.addAttribute("movies", movieService.getAllMovies());
        return "admin/movies";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("movie", MovieDto.builder().build());
        return "admin/movie-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Movie movie = movieService.getMovieById(id);
        MovieDto dto = MovieDto.builder()
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

        model.addAttribute("movie", dto);
        return "admin/movie-form";
    }

    @PostMapping("/save")
    public String saveMovie(@Valid @ModelAttribute("movie") MovieDto movieDto,
                            BindingResult result,
                            @RequestParam(value = "image", required = false) MultipartFile image,
                            @RequestParam(value = "gallery", required = false) List<MultipartFile> gallery) {
        if (result.hasErrors()) {
            return "admin/movie-form";
        }

        String posterUrl = movieDto.posterUrl();
        if (image != null && !image.isEmpty()) {
            posterUrl = movieService.storeFile(image);
        }

        Movie details = Movie.builder()
                .title(movieDto.title())
                .description(movieDto.description())
                .genre(movieDto.genre())
                .director(movieDto.director())
                .durationMinutes(movieDto.durationMinutes())
                .ageRestriction(movieDto.ageRestriction())
                .posterUrl(posterUrl)
                .trailerUrl(movieDto.trailerUrl())
                .castMembers(movieDto.castMembers())
                .build();

        if (movieDto.id() != null) {
            movieService.updateMovie(movieDto.id(), details, gallery);
        } else {
            movieService.addMovie(details, gallery);
        }

        return "redirect:/admin/movies";
    }

    @GetMapping("/delete/{id}")
    public String deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return "redirect:/admin/movies";
    }
}