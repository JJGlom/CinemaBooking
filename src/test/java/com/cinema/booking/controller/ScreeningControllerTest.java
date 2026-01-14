package com.cinema.booking.controller;

import com.cinema.booking.dto.CreateScreeningDto;
import com.cinema.booking.dto.ScreeningDto;
import com.cinema.booking.service.ScreeningService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ScreeningController.class)
class ScreeningControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScreeningService screeningService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void shouldCreateScreening() throws Exception {
        CreateScreeningDto request = new CreateScreeningDto(1L, 1L, LocalDateTime.now().plusDays(1));
        ScreeningDto response = ScreeningDto.builder().id(100L).movieTitle("Test").build();

        when(screeningService.createScreening(any(CreateScreeningDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/screenings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L));
    }

    @Test
    @WithMockUser
    void shouldGetScreeningsByDate() throws Exception {
        LocalDate date = LocalDate.now();
        ScreeningDto screening = ScreeningDto.builder().id(100L).movieTitle("Test").build();

        when(screeningService.getScreeningsByDate(date)).thenReturn(List.of(screening));

        mockMvc.perform(get("/api/v1/screenings")
                        .param("date", date.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100L));
    }
}