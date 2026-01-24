package com.cinema.booking.controller;

import com.cinema.booking.config.SecurityConfig;
import com.cinema.booking.model.*;
import com.cinema.booking.service.BookingService;
import com.cinema.booking.service.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingWebController.class)
@Import(SecurityConfig.class)
class BookingWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @Test
    @WithMockUser
    void shouldShowEmptyCartWhenSessionIsNull() throws Exception {
        mockMvc.perform(get("/booking/cart"))
                .andExpect(status().isOk())
                .andExpect(view().name("booking-cart"))
                .andExpect(model().attribute("totalPrice", BigDecimal.ZERO));
    }

    @Test
    @WithMockUser
    void shouldShowEmptyCartWhenSessionAttributeIsEmpty() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("cartTickets", Collections.emptyList());

        mockMvc.perform(get("/booking/cart").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("booking-cart"))
                .andExpect(model().attribute("totalPrice", BigDecimal.ZERO));
    }

    @Test
    @WithMockUser
    void shouldShowCartWithTickets() throws Exception {
        MockHttpSession session = new MockHttpSession();
        List<Long> ids = List.of(1L);
        session.setAttribute("cartTickets", ids);

        Ticket ticket = Ticket.builder()
                .id(1L)
                .price(BigDecimal.TEN)
                .purchaseDate(LocalDateTime.now())
                .screening(Screening.builder().movie(new Movie()).room(new Room()).build())
                .seat(Seat.builder().rowNumber(1).seatNumber(1).build())
                .type(TicketType.NORMAL)
                .build();

        when(bookingService.getTicketsByIds(ids)).thenReturn(List.of(ticket));

        mockMvc.perform(get("/booking/cart").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute("tickets", List.of(ticket)))
                .andExpect(model().attributeExists("secondsRemaining"));
    }

    @Test
    @WithMockUser
    void shouldHandleCartInconsistency() throws Exception {
        MockHttpSession session = new MockHttpSession();
        List<Long> ids = List.of(1L, 2L);
        session.setAttribute("cartTickets", ids);

        Ticket ticket = Ticket.builder().id(1L).build();
        when(bookingService.getTicketsByIds(ids)).thenReturn(List.of(ticket));

        mockMvc.perform(get("/booking/cart").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/booking/cart?expired"));

        assertThat(session.getAttribute("cartTickets")).isNull();
    }

    @Test
    @WithMockUser
    void shouldHandleExpiredCart() throws Exception {
        MockHttpSession session = new MockHttpSession();
        List<Long> ids = List.of(1L);
        session.setAttribute("cartTickets", ids);

        Ticket ticket = Ticket.builder()
                .id(1L)
                .price(BigDecimal.TEN)
                .purchaseDate(LocalDateTime.now().minusMinutes(2))
                .build();

        when(bookingService.getTicketsByIds(ids)).thenReturn(List.of(ticket));

        mockMvc.perform(get("/booking/cart").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/booking/cart?expired"));

        verify(bookingService).cancelReservation(ids);
    }

    @Test
    @WithMockUser
    void shouldEditBooking() throws Exception {
        MockHttpSession session = new MockHttpSession();
        List<Long> ids = List.of(1L);
        session.setAttribute("cartTickets", ids);

        Screening screening = Screening.builder().id(10L).build();
        Seat seat = Seat.builder().id(5L).build();
        Ticket ticket = Ticket.builder()
                .id(1L)
                .screening(screening)
                .seat(seat)
                .type(TicketType.NORMAL)
                .build();

        when(bookingService.getTicketsByIds(ids)).thenReturn(List.of(ticket));

        mockMvc.perform(get("/booking/edit").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/booking/screening/10?edit=true&seats=5&types=NORMAL"));

        verify(bookingService).cancelReservation(ids);
    }

    @Test
    @WithMockUser
    void shouldRedirectEditWhenSessionIsNull() throws Exception {
        mockMvc.perform(get("/booking/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @WithMockUser
    void shouldRedirectEditWhenSessionListIsEmpty() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("cartTickets", Collections.emptyList());

        mockMvc.perform(get("/booking/edit").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @WithMockUser
    void shouldRedirectEditWhenTicketsEmpty() throws Exception {
        MockHttpSession session = new MockHttpSession();
        List<Long> ids = List.of(1L);
        session.setAttribute("cartTickets", ids);

        when(bookingService.getTicketsByIds(ids)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/booking/edit").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @WithMockUser
    void shouldHandleCancelBookingWithNullSession() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(get("/booking/cancel").session(session))
                .andExpect(status().is3xxRedirection());

        verify(bookingService, never()).cancelReservation(anyList());
    }

    @Test
    @WithMockUser
    void shouldHandleCancelBookingWithIds() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("cartTickets", List.of(1L));

        mockMvc.perform(get("/booking/cancel").session(session))
                .andExpect(status().is3xxRedirection());

        verify(bookingService).cancelReservation(anyList());
    }

    @Test
    @WithMockUser
    void shouldRedirectSuccessWhenNoTicketsFound() throws Exception {
        when(bookingService.getTicketsByIds(anyList())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/booking/success").param("ids", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @WithMockUser
    void shouldShowBookingSuccess() throws Exception {
        Ticket ticket = Ticket.builder()
                .id(1L)
                .price(BigDecimal.TEN)
                .orderId("ORD")
                .screening(Screening.builder().movie(new Movie()).room(new Room()).startTime(LocalDateTime.now()).build())
                .seat(new Seat())
                .build();
        when(bookingService.getTicketsByIds(anyList())).thenReturn(List.of(ticket));

        mockMvc.perform(get("/booking/success").param("ids", "1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void shouldShowSeatSelectionPage() throws Exception {
        mockMvc.perform(get("/booking/screening/1"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("screeningId", 1L))
                .andExpect(view().name("booking-seats"));
    }
}