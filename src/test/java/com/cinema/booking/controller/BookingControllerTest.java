package com.cinema.booking.controller;

import com.cinema.booking.config.SecurityConfig;
import com.cinema.booking.dto.BookTicketDto;
import com.cinema.booking.dto.SeatDto;
import com.cinema.booking.dto.TicketSelection;
import com.cinema.booking.model.TicketType;
import com.cinema.booking.service.BookingService;
import com.cinema.booking.service.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
@Import(SecurityConfig.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void shouldCreateBookingAndReturnCreatedStatus() throws Exception {
        TicketSelection ts1 = new TicketSelection(10L, TicketType.NORMAL);
        TicketSelection ts2 = new TicketSelection(11L, TicketType.REDUCED);

        BookTicketDto request = new BookTicketDto(1L, List.of(ts1, ts2));
        List<Long> createdIds = List.of(100L, 101L);

        when(bookingService.createReservation(any(BookTicketDto.class))).thenReturn(createdIds);

        mockMvc.perform(post("/api/v1/bookings/reserve")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequestWhenInputIsInvalid() throws Exception {
        BookTicketDto invalidRequest = new BookTicketDto(null, List.of());

        mockMvc.perform(post("/api/v1/bookings/reserve")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void shouldGetSeatsForScreening() throws Exception {
        Long screeningId = 1L;
        SeatDto seat = SeatDto.builder().id(10L).available(true).build();
        when(bookingService.getSeatsForScreening(screeningId)).thenReturn(List.of(seat));

        mockMvc.perform(get("/api/v1/bookings/screening/{id}/seats", screeningId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10L));
    }

    @Test
    @WithMockUser
    void shouldPayForTickets() throws Exception {
        List<Long> ticketIds = List.of(1L, 2L);

        mockMvc.perform(post("/api/v1/bookings/pay")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ticketIds)))
                .andExpect(status().isOk());

        verify(bookingService).confirmPayment(ticketIds);
    }
}