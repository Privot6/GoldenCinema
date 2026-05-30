package com.goldencinema.backend.controller;

import com.goldencinema.backend.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Kontroler panelu admina do generowania raportów finansowych.
 * Raporty są generowane i zwracane jako pliki PDF.
 */
@RestController
@RequestMapping("/api/admin/reports")
public class AdminReportController {

    private final ReportService reportService;

    public AdminReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Generuje raport zysku tygodniowego dla podanego zakresu dat.
     *
     * @param from data początkowa zakresu (format ISO: YYYY-MM-DD)
     * @param to   data końcowa zakresu (format ISO: YYYY-MM-DD)
     * @return plik PDF z raportem jako załącznik do pobrania
     */
    @GetMapping("/weekly-profit")
    public ResponseEntity<byte[]> getWeeklyProfitReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        byte[] pdf = reportService.generateWeeklyProfitPdf(from, to);
        String filename = "raport_" + from + "_" + to + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(pdf);
    }
}