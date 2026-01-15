package com.cinema.booking.controller;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.dao.DataIntegrityViolationException;
import com.cinema.booking.dto.CreateScreeningDto;
import com.cinema.booking.service.MovieService;
import com.cinema.booking.service.ScreeningService;
import com.cinema.booking.repository.RoomRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/screenings")
@RequiredArgsConstructor
public class AdminScreeningController {

    private final ScreeningService screeningService;
    private final MovieService movieService;
    private final RoomRepository roomRepository;

    @GetMapping
    public String listScreenings(@RequestParam(required = false) LocalDate date, Model model) {
        LocalDate selectedDate = (date != null) ? date : LocalDate.now();
        model.addAttribute("screenings", screeningService.getScreeningsByDate(selectedDate));
        model.addAttribute("selectedDate", selectedDate);
        return "admin/screenings";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("screening", new CreateScreeningDto(null, null, null));
        model.addAttribute("movies", movieService.getAllMovies());
        model.addAttribute("rooms", roomRepository.findAll());
        return "admin/screening-form";
    }

    @PostMapping("/add")
    public String addScreening(@Valid @ModelAttribute("screening") CreateScreeningDto dto,
                               BindingResult result,
                               Model model) {
        if (result.hasErrors()) {
            model.addAttribute("movies", movieService.getAllMovies());
            model.addAttribute("rooms", roomRepository.findAll());
            return "admin/screening-form";
        }

        try {
            screeningService.createScreening(dto);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage()); // Np. "Sala zajęta"
            model.addAttribute("movies", movieService.getAllMovies());
            model.addAttribute("rooms", roomRepository.findAll());
            return "admin/screening-form";
        }

        return "redirect:/admin/screenings";
    }

    @GetMapping("/delete/{id}")
    public String deleteScreening(@PathVariable Long id,
                                  @RequestParam(required = false) LocalDate date,
                                  RedirectAttributes redirectAttributes) {
        try {
            screeningService.deleteScreening(id);
            redirectAttributes.addFlashAttribute("success", "Seans został pomyślnie usunięty.");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("error", "Nie można usunąć seansu, na który zostały już sprzedane bilety!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Wystąpił nieoczekiwany błąd podczas usuwania.");
        }

        String redirectUrl = "redirect:/admin/screenings";
        if (date != null) {
            redirectUrl += "?date=" + date;
        }
        return redirectUrl;
    }
}