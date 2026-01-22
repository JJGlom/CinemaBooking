package com.cinema.booking.controller;

import com.cinema.booking.model.Ticket;
import com.cinema.booking.service.BookingService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
            model.addAttribute("secondsRemaining", 0);
            return "booking-cart";
        }

        List<Ticket> tickets = bookingService.getTicketsByIds(ids);

        if (tickets.size() != ids.size()) {
            session.removeAttribute("cartTickets");
            return "redirect:/booking/cart?expired";
        }

        long secondsSincePurchase = 0;
        if (!tickets.isEmpty()) {
            secondsSincePurchase = Duration.between(tickets.get(0).getPurchaseDate(), LocalDateTime.now()).toSeconds();
        }
        long secondsRemaining = Math.max(0, 60 - secondsSincePurchase);

        if (secondsRemaining == 0) {
            bookingService.cancelReservation(ids);
            session.removeAttribute("cartTickets");
            return "redirect:/booking/cart?expired";
        }

        BigDecimal totalPrice = tickets.stream()
                .map(Ticket::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("tickets", tickets);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("ticketIds", ids);
        model.addAttribute("secondsRemaining", secondsRemaining);

        return "booking-cart";
    }

    @GetMapping("/edit")
    public String editBooking(HttpSession session) {
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) session.getAttribute("cartTickets");

        if (ids == null || ids.isEmpty()) {
            return "redirect:/";
        }

        List<Ticket> tickets = bookingService.getTicketsByIds(ids);
        if (tickets.isEmpty()) {
            return "redirect:/";
        }

        Long screeningId = tickets.get(0).getScreening().getId();

        String seatIds = tickets.stream()
                .map(t -> String.valueOf(t.getSeat().getId()))
                .collect(Collectors.joining(","));

        String types = tickets.stream()
                .map(t -> t.getType().toString())
                .collect(Collectors.joining(","));

        bookingService.cancelReservation(ids);
        session.removeAttribute("cartTickets");

        return "redirect:/booking/screening/" + screeningId + "?edit=true&seats=" + seatIds + "&types=" + types;
    }

    @GetMapping("/success")
    public String bookingSuccess(@RequestParam List<Long> ids, Model model) {
        List<Ticket> tickets = bookingService.getTicketsByIds(ids);

        if (tickets.isEmpty()) {
            return "redirect:/";
        }

        BigDecimal totalPrice = tickets.stream()
                .map(Ticket::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("tickets", tickets);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("orderId", tickets.get(0).getOrderId());
        return "booking-success";
    }

    @GetMapping("/cancel")
    public String cancelBooking(HttpSession session) {
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) session.getAttribute("cartTickets");

        if (ids != null && !ids.isEmpty()) {
            bookingService.cancelReservation(ids);
        }

        session.removeAttribute("cartTickets");
        return "redirect:/";
    }
}