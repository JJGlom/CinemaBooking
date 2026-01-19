package com.cinema.booking.controller;

import com.cinema.booking.model.Ticket;
import com.cinema.booking.service.BookingService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/booking")
@RequiredArgsConstructor
public class BookingWebController {

    private final BookingService bookingService;

    @GetMapping("/screening/{screeningId}")
    public String showSeatSelection(@PathVariable Long screeningId, Model model) {
        model.addAttribute("screeningId", screeningId);
        return "booking-seats";
    }

    @GetMapping("/cart")
    public String showCart(HttpSession session, Model model) {
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) session.getAttribute("cartTickets");

        if (ids == null || ids.isEmpty()) {
            model.addAttribute("tickets", Collections.emptyList());
            model.addAttribute("totalPrice", BigDecimal.ZERO);
            model.addAttribute("ticketIds", Collections.emptyList());
            return "booking-cart";
        }

        List<Ticket> tickets = bookingService.getTicketsByIds(ids);

        if (tickets.size() != ids.size()) {
            session.removeAttribute("cartTickets");
            return "redirect:/booking/cart?expired";
        }

        BigDecimal totalPrice = tickets.stream()
                .map(Ticket::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("tickets", tickets);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("ticketIds", ids);
        return "booking-cart";
    }

    @GetMapping("/success")
    public String bookingSuccess(@RequestParam List<Long> ids, Model model) {
        List<Ticket> tickets = bookingService.getTicketsByIds(ids);

        BigDecimal totalPrice = tickets.stream()
                .map(Ticket::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("tickets", tickets);
        model.addAttribute("totalPrice", totalPrice);
        return "booking-success";
    }

    @GetMapping("/cancel")
    public String cancelBooking(HttpSession session) {
        session.removeAttribute("cartTickets");
        return "redirect:/";
    }
}