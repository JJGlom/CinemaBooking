package com.cinema.booking.controller;

import com.cinema.booking.model.Movie;
import com.cinema.booking.model.Screening;
import com.cinema.booking.model.Seat;
import com.cinema.booking.model.Ticket;
import com.cinema.booking.service.BookingService;
import com.cinema.booking.service.ScreeningService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {RepertoireWebController.class, BookingWebController.class})
class WebControllersTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScreeningService screeningService;

    @MockBean
    private BookingService bookingService;

    @Test
    @WithMockUser
    void shouldShowRepertoirePage() throws Exception {
        when(screeningService.getScreeningsByDate(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("repertoire"))
                .andExpect(model().attributeExists("screenings", "selectedDate"));
    }

    @Test
    @WithMockUser
    void shouldShowSeatSelectionPage() throws Exception {
        Long screeningId = 1L;

        mockMvc.perform(get("/booking/screening/{id}", screeningId))
                .andExpect(status().isOk())
                .andExpect(view().name("booking-seats"))
                .andExpect(model().attribute("screeningId", screeningId));
    }

    @Test
    @WithMockUser
    void shouldShowSuccessPageWithTickets() throws Exception {
        Movie movie = Movie.builder().title("Dune").build();
        Screening screening = Screening.builder().movie(movie).startTime(LocalDateTime.now()).build();
        Seat seat = Seat.builder().rowNumber(1).seatNumber(5).build();

        Ticket ticket = Ticket.builder()
                .id(100L)
                .price(BigDecimal.valueOf(25.00))
                .screening(screening)
                .seat(seat)
                .build();

        when(bookingService.getTicketsByIds(List.of(100L))).thenReturn(List.of(ticket));

        mockMvc.perform(get("/booking/success").param("ids", "100"))
                .andExpect(status().isOk())
                .andExpect(view().name("booking-success"))
                .andExpect(model().attributeExists("tickets", "totalPrice"))
                .andExpect(model().attribute("totalPrice", BigDecimal.valueOf(25.00)));
    }
}