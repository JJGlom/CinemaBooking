package com.cinema.booking.exception;

import com.cinema.booking.config.SecurityConfig;
import com.cinema.booking.controller.MovieController;
import com.cinema.booking.service.CustomUserDetailsService;
import com.cinema.booking.service.MovieService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MovieController.class)
@Import(SecurityConfig.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MovieService movieService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @Test
    @WithMockUser
    void shouldHandleResourceNotFoundException() throws Exception {
        when(movieService.getMovieById(1L)).thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(get("/api/v1/movies/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Not found"));
    }

    @Test
    @WithMockUser
    void shouldHandleIllegalArgumentException() throws Exception {
        when(movieService.getMovieById(1L)).thenThrow(new IllegalArgumentException("Bad request"));

        mockMvc.perform(get("/api/v1/movies/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Bad request"));
    }
}