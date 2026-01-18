package com.cinema.booking.controller;

import com.cinema.booking.dto.MovieDto;
import com.cinema.booking.model.Movie;
import com.cinema.booking.service.MovieService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MovieController.class)
class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MovieService movieService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void shouldReturnAllMoviesPaged() throws Exception {
        Movie movie = Movie.builder().id(1L).title("Test Movie").genre("Action").build();
        Page<Movie> page = new PageImpl<>(List.of(movie));

        when(movieService.getAllMovies(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/movies")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Test Movie"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser
    void shouldReturnEmptyPageWhenNoMovies() throws Exception {
        when(movieService.getAllMovies(any(Pageable.class))).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    @WithMockUser
    void shouldCreateMovie() throws Exception {
        MovieDto dto = MovieDto.builder().title("New Movie").genre("Drama").director("Me").durationMinutes(100).build();
        Movie savedMovie = Movie.builder().id(1L).title("New Movie").genre("Drama").build();

        when(movieService.addMovie(any(Movie.class))).thenReturn(savedMovie);

        mockMvc.perform(post("/api/v1/movies")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser
    void shouldUpdateMovie() throws Exception {
        Long movieId = 1L;
        MovieDto dto = MovieDto.builder().title("Updated Title").genre("Drama").director("Director").durationMinutes(120).build();
        Movie updatedMovie = Movie.builder().id(movieId).title("Updated Title").genre("Drama").build();

        when(movieService.updateMovie(eq(movieId), any(Movie.class))).thenReturn(updatedMovie);

        mockMvc.perform(put("/api/v1/movies/{id}", movieId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    @WithMockUser
    void shouldDeleteMovie() throws Exception {
        mockMvc.perform(delete("/api/v1/movies/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}