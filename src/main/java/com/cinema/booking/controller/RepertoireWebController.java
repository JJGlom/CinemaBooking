package com.cinema.booking.controller;

import com.cinema.booking.dto.ScreeningDto;
import com.cinema.booking.service.ScreeningService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class RepertoireWebController {

    private final ScreeningService screeningService;

    @GetMapping("/")
    public String showRepertoire(@RequestParam(required = false) LocalDate date, Model model) {
        LocalDate selectedDate = (date != null) ? date : LocalDate.now();
        List<ScreeningDto> screenings = screeningService.getScreeningsByDate(selectedDate);

        model.addAttribute("screenings", screenings);
        model.addAttribute("selectedDate", selectedDate);

        return "repertoire";
    }
}