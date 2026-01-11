package com.cinema.booking.controller;

import com.cinema.booking.dto.CreateScreeningDto;
import com.cinema.booking.dto.ScreeningDto;
import com.cinema.booking.service.ScreeningService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/screenings")
@RequiredArgsConstructor
public class ScreeningController {

    private final ScreeningService screeningService;

    @PostMapping
    public ResponseEntity<ScreeningDto> createScreening(@Valid @RequestBody CreateScreeningDto dto) {
        return new ResponseEntity<>(screeningService.createScreening(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ScreeningDto>> getScreeningsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(screeningService.getScreeningsByDate(date));
    }
}