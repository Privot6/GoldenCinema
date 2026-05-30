package com.goldencinema.backend.controller;

import com.goldencinema.backend.dto.ScreeningResponse;
import com.goldencinema.backend.dto.SeatRowDto;
import com.goldencinema.backend.service.ScreeningService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Kontroler publicznych endpointów repertuaru i dostępności miejsc.
 * Nie wymaga uwierzytelnienia — dane są dostępne dla wszystkich użytkowników.
 */
@RestController
public class ScreeningController {

    private final ScreeningService screeningService;

    public ScreeningController(ScreeningService screeningService) {
        this.screeningService = screeningService;
    }

    /**
     * Zwraca listę nadchodzących seansów.
     *
     * @return lista seansów zaplanowanych od bieżącego momentu
     */
    @GetMapping({"/screenings", "/api/screenings"})
    public List<ScreeningResponse> getUpcomingScreenings() {
        return screeningService.getUpcomingScreenings();
    }

    /**
     * Zwraca układ miejsc wraz z dostępnością dla danego seansu.
     *
     * @param screeningId identyfikator seansu
     * @return lista rzędów z miejscami i ich statusem (dostępne/zajęte)
     */
    @GetMapping("/api/screenings/{screeningId}/seats")
    public List<SeatRowDto> getSeatAvailabilityForScreening(@PathVariable Long screeningId) {
        return screeningService.getSeatAvailabilityForScreening(screeningId);
    }

    /**
     * Zwraca nadchodzące seanse dla konkretnego filmu.
     *
     * @param movieId identyfikator filmu
     * @return lista seansów danego filmu zaplanowanych od bieżącego momentu
     */
    @GetMapping("/api/movies/{movieId}/screenings")
    public List<ScreeningResponse> getUpcomingScreeningsByMovieId(@PathVariable Long movieId) {
        return screeningService.getUpcomingScreeningsByMovieId(movieId);
    }
}