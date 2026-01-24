package com.cinema.booking.controller;

import com.cinema.booking.dto.CreateScreeningDto;
import com.cinema.booking.model.Movie;
import com.cinema.booking.model.Room;
import com.cinema.booking.service.MovieService;
import com.cinema.booking.service.RoomService;
import com.cinema.booking.service.ScreeningService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminScreeningController.class)
class AdminScreeningControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScreeningService screeningService;

    @MockBean
    private MovieService movieService;

    @MockBean
    private RoomService roomService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldListScreeningsWithoutDateParam() throws Exception {
        when(screeningService.getScreeningsByDate(any(LocalDate.class))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/screenings"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/screenings"))
                .andExpect(model().attributeExists("screenings", "selectedDate"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldListScreeningsWithDateParam() throws Exception {
        when(screeningService.getScreeningsByDate(any(LocalDate.class))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/screenings").param("date", "2025-01-01"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("selectedDate"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldShowAddForm() throws Exception {
        when(movieService.getAllMovies()).thenReturn(List.of(new Movie()));
        when(roomService.getAllRooms()).thenReturn(List.of(new Room()));

        mockMvc.perform(get("/admin/screenings/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/screening-form"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAddScreeningSuccess() throws Exception {
        mockMvc.perform(post("/admin/screenings/add")
                        .with(csrf())
                        .param("movieId", "1")
                        .param("roomId", "1")
                        .param("startTime", "2030-01-01T20:00"))
                .andExpect(status().is3xxRedirection());

        verify(screeningService).createScreening(any(CreateScreeningDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldHandleValidationErrorsInAddScreening() throws Exception {
        mockMvc.perform(post("/admin/screenings/add")
                        .with(csrf())
                        .param("movieId", "")
                        .param("startTime", ""))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("movies"))
                .andExpect(model().attributeExists("rooms"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldHandleServiceExceptionInAddScreening() throws Exception {
        doThrow(new IllegalArgumentException("Conflict")).when(screeningService).createScreening(any());

        mockMvc.perform(post("/admin/screenings/add")
                        .with(csrf())
                        .param("movieId", "1")
                        .param("roomId", "1")
                        .param("startTime", "2030-01-01T20:00"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attributeExists("movies"))
                .andExpect(model().attributeExists("rooms"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteScreeningSuccessWithoutDateParam() throws Exception {
        mockMvc.perform(get("/admin/screenings/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/screenings"))
                .andExpect(flash().attributeExists("success"));

        verify(screeningService).deleteScreening(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteScreeningSuccessWithDateParam() throws Exception {
        String date = "2025-05-20";
        mockMvc.perform(get("/admin/screenings/delete/1").param("date", date))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/screenings?date=" + date));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldHandleDeleteDataIntegrityViolation() throws Exception {
        doThrow(new DataIntegrityViolationException("fk constraint")).when(screeningService).deleteScreening(1L);

        mockMvc.perform(get("/admin/screenings/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("error"))
                .andExpect(flash().attribute("error", "Nie można usunąć seansu, na który zostały już sprzedane bilety!"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldHandleDeleteGeneralException() throws Exception {
        doThrow(new RuntimeException("Error")).when(screeningService).deleteScreening(1L);

        mockMvc.perform(get("/admin/screenings/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("error"))
                .andExpect(flash().attribute("error", "Wystąpił nieoczekiwany błąd podczas usuwania."));
    }
}