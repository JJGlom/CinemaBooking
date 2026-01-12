package com.cinema.booking.service;

import com.cinema.booking.dto.MovieStatsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final JdbcTemplate jdbcTemplate;

    public List<MovieStatsDto> getSalesStats() {
        String sql = """
            SELECT 
                m.title as movie_title, 
                COUNT(t.id) as tickets_sold, 
                COALESCE(SUM(t.price), 0) as total_revenue
            FROM movies m
            LEFT JOIN screenings s ON s.movie_id = m.id
            LEFT JOIN tickets t ON t.screening_id = s.id
            GROUP BY m.id, m.title
            ORDER BY total_revenue DESC
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new MovieStatsDto(
                rs.getString("movie_title"),
                rs.getLong("tickets_sold"),
                rs.getBigDecimal("total_revenue")
        ));
    }
}