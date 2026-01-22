package com.cinema.booking.service;

import com.cinema.booking.repository.TicketJdbcDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketCleanupService {

    private final TicketJdbcDao ticketJdbcDao;

    @Scheduled(fixedRate = 10000)
    @Transactional
    public void cleanupUnpaidTickets() {
        log.debug("Uruchamianie procesu czyszczenia nieopłaconych biletów");
        ticketJdbcDao.deleteUnpaidTicketsOlderThan(1);
    }
}