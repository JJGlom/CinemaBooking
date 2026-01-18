package com.cinema.booking.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TicketJdbcDao {

    private final JdbcTemplate jdbcTemplate;

    public void markTicketAsPaid(Long ticketId) {
        String sql = "UPDATE tickets SET paid = true WHERE id = ?";
        jdbcTemplate.update(sql, ticketId);
    }

    public void deleteUnpaidTicketsOlderThan(int minutes) {
        String sql = "DELETE FROM tickets WHERE paid = false AND purchase_date < CURRENT_TIMESTAMP - INTERVAL '" + minutes + "' MINUTE";
        jdbcTemplate.update(sql);
    }
}