package com.cinema.booking.repository;

import com.cinema.booking.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Optional<Ticket> findByTicketIdentifier(String ticketIdentifier);
    List<Ticket> findByScreeningId(Long screeningId);
}