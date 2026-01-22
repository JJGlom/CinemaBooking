package com.cinema.booking.controller;

import com.cinema.booking.dto.DailyStatsDto;
import com.cinema.booking.dto.MovieStatsDto;
import com.cinema.booking.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.PrintWriter;
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

    @GetMapping(value = "/sales/csv", produces = "text/csv")
    public void exportSalesStatsToCsv(HttpServletResponse response) throws IOException {
        List<MovieStatsDto> movieStats = reportService.getSalesStats();
        List<DailyStatsDto> dailyStats = reportService.getDailyStats();

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"raport_sprzedazy.csv\"");

        try (PrintWriter writer = response.getWriter()) {
            writer.write('\uFEFF');

            writer.println("--- RAPORT WG FILMOW ---");
            writer.println("Tytul Filmu;Sprzedane Bilety;Przychod");
            for (MovieStatsDto stat : movieStats) {
                writer.printf("%s;%d;%s%n",
                        escapeCsv(stat.movieTitle()),
                        stat.ticketsSold(),
                        stat.totalRevenue().toString().replace('.', ','));
            }

            writer.println();
            writer.println("--- RAPORT DZIENNY ---");
            writer.println("Data;Sprzedane Bilety;Przychod");
            for (DailyStatsDto stat : dailyStats) {
                writer.printf("%s;%d;%s%n",
                        stat.date(),
                        stat.ticketsSold(),
                        stat.totalRevenue().toString().replace('.', ','));
            }
        }
    }

    private String escapeCsv(String data) {
        if (data == null) return "";
        if (data.contains(";") || data.contains("\"") || data.contains("\n")) {
            return "\"" + data.replace("\"", "\"\"") + "\"";
        }
        return data;
    }
}