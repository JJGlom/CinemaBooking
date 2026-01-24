package com.cinema.booking.service;

import com.cinema.booking.repository.TicketJdbcDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TicketCleanupServiceTest {

    @Mock
    private TicketJdbcDao ticketJdbcDao;

    @InjectMocks
    private TicketCleanupService ticketCleanupService;

    @Test
    void shouldCleanupUnpaidTickets() {
        ticketCleanupService.cleanupUnpaidTickets();
        verify(ticketJdbcDao).deleteUnpaidTicketsOlderThan(1);
    }
}