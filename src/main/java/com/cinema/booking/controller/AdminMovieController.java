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

    @PostMapping("/add")
    public String addMovie(@Valid @ModelAttribute("movie") MovieDto movieDto, BindingResult result) {
        if (result.hasErrors()) {
            return "admin/movie-form";
        }

        Movie movie = Movie.builder()
                .title(movieDto.title())
                .description(movieDto.description())
                .genre(movieDto.genre())
                .director(movieDto.director())
                .durationMinutes(movieDto.durationMinutes())
                .ageRestriction(movieDto.ageRestriction())
                .posterUrl(movieDto.posterUrl())
                .trailerUrl(movieDto.trailerUrl())
                .castMembers(movieDto.castMembers())
                .build();

        movieService.addMovie(movie);
        return "redirect:/admin/movies";
    }

    @GetMapping("/delete/{id}")
    public String deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return "redirect:/admin/movies";
    }
}