package com.cinema.booking.controller;

import com.cinema.booking.dto.MovieStatsDto;
import com.cinema.booking.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

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
}