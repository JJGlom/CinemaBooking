package com.cinema.booking.controller;

import com.cinema.booking.dto.MovieStatsDto;
import com.cinema.booking.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/sales")
    public ResponseEntity<List<MovieStatsDto>> getSalesStats() {
        return ResponseEntity.ok(reportService.getSalesStats());
    }
}