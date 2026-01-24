package com.cinema.booking.controller;

import com.cinema.booking.config.SecurityConfig;
import com.cinema.booking.dto.DailyStatsDto;
import com.cinema.booking.dto.MovieStatsDto;
import com.cinema.booking.service.CustomUserDetailsService;
import com.cinema.booking.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
@Import(SecurityConfig.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @Test
    @WithMockUser
    void shouldReturnSalesStats() throws Exception {
        MovieStatsDto stats = new MovieStatsDto("Dune", 100L, BigDecimal.valueOf(2500));
        when(reportService.getSalesStats()).thenReturn(List.of(stats));

        mockMvc.perform(get("/api/v1/reports/sales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].movieTitle").value("Dune"))
                .andExpect(jsonPath("$[0].totalRevenue").value(2500));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldExportCsvWithSpecialCharacters() throws Exception {
        MovieStatsDto trickyMovie = new MovieStatsDto("Film; with \"quotes\" and \n newline", 10L, BigDecimal.TEN);
        DailyStatsDto dailyStat = new DailyStatsDto(LocalDate.of(2025, 1, 1), 5L, BigDecimal.ONE);

        when(reportService.getSalesStats()).thenReturn(List.of(trickyMovie));
        when(reportService.getDailyStats()).thenReturn(List.of(dailyStat));

        mockMvc.perform(get("/api/v1/reports/sales/csv"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv; charset=UTF-8"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"raport_sprzedazy.csv\""))
                .andExpect(content().string(containsString("\"Film; with \"\"quotes\"\" and \n newline\"")))
                .andExpect(content().string(containsString("2025-01-01;5;1")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldExportCsvWithNullAndSimpleData() throws Exception {
        MovieStatsDto nullTitleMovie = new MovieStatsDto(null, 5L, BigDecimal.ONE);
        MovieStatsDto simpleMovie = new MovieStatsDto("Simple Title", 2L, BigDecimal.TEN);

        when(reportService.getSalesStats()).thenReturn(Arrays.asList(nullTitleMovie, simpleMovie));
        when(reportService.getDailyStats()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/reports/sales/csv"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(";5;1")))
                .andExpect(content().string(containsString("Simple Title;2;10")));
    }
}