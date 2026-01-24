package com.cinema.booking.service;

import com.cinema.booking.dto.DailyStatsDto;
import com.cinema.booking.dto.MovieStatsDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private ReportService reportService;

    @Test
    void shouldGetSalesStats() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenAnswer(invocation -> {
            RowMapper<MovieStatsDto> mapper = invocation.getArgument(1);
            ResultSet rs = mock(ResultSet.class);
            when(rs.getString("movie_title")).thenReturn("Dune");
            when(rs.getLong("tickets_sold")).thenReturn(100L);
            when(rs.getBigDecimal("total_revenue")).thenReturn(BigDecimal.valueOf(2500));
            return List.of(mapper.mapRow(rs, 1));
        });

        List<MovieStatsDto> result = reportService.getSalesStats();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).movieTitle()).isEqualTo("Dune");
        assertThat(result.get(0).ticketsSold()).isEqualTo(100L);
        assertThat(result.get(0).totalRevenue()).isEqualTo(BigDecimal.valueOf(2500));
    }

    @Test
    void shouldGetDailyStats() {
        LocalDate today = LocalDate.now();
        when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenAnswer(invocation -> {
            RowMapper<DailyStatsDto> mapper = invocation.getArgument(1);
            ResultSet rs = mock(ResultSet.class);
            when(rs.getDate("sale_date")).thenReturn(Date.valueOf(today));
            when(rs.getLong("tickets_sold")).thenReturn(50L);
            when(rs.getBigDecimal("total_revenue")).thenReturn(BigDecimal.valueOf(1000));
            return List.of(mapper.mapRow(rs, 1));
        });

        List<DailyStatsDto> result = reportService.getDailyStats();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).date()).isEqualTo(today);
        assertThat(result.get(0).ticketsSold()).isEqualTo(50L);
        assertThat(result.get(0).totalRevenue()).isEqualTo(BigDecimal.valueOf(1000));
    }
}