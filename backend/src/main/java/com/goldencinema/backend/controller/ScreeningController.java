package com.goldencinema.backend.controller;

import com.goldencinema.backend.dto.ScreeningResponse;
import com.goldencinema.backend.service.ScreeningService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ScreeningController {

    private final ScreeningService screeningService;

    public ScreeningController(ScreeningService screeningService) {
        this.screeningService = screeningService;
    }

    @GetMapping("/screenings")
    public List<ScreeningResponse> getUpcomingScreenings() {
        return screeningService.getUpcomingScreenings();
    }

    @GetMapping("/movies/{movieId}/screenings")
    public List<ScreeningResponse> getUpcomingScreeningsByMovieId(@PathVariable Long movieId) {
        return screeningService.getUpcomingScreeningsByMovieId(movieId);
    }
}