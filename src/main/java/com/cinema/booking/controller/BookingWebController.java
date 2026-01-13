package com.cinema.booking.controller;

import com.cinema.booking.model.Ticket;
import com.cinema.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
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
}