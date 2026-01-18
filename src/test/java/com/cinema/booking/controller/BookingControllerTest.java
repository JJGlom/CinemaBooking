package com.cinema.booking.controller;

import com.cinema.booking.dto.BookTicketDto;
import com.cinema.booking.dto.TicketSelection;
import com.cinema.booking.model.TicketType;
import com.cinema.booking.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void shouldCreateBookingAndReturnCreatedStatus() throws Exception {
        TicketSelection ts1 = new TicketSelection(10L, TicketType.NORMAL);
        TicketSelection ts2 = new TicketSelection(11L, TicketType.REDUCED);

        BookTicketDto request = new BookTicketDto(1L, List.of(ts1, ts2));
        List<Long> createdIds = List.of(100L, 101L);

        when(bookingService.bookTicket(any(BookTicketDto.class))).thenReturn(createdIds);

        mockMvc.perform(post("/api/v1/bookings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequestWhenInputIsInvalid() throws Exception {
        BookTicketDto invalidRequest = new BookTicketDto(null, List.of());

        mockMvc.perform(post("/api/v1/bookings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}