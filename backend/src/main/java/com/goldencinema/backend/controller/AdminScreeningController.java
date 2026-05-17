package com.goldencinema.backend.controller;

import com.goldencinema.backend.dto.CreateScreeningRequest;
import com.goldencinema.backend.dto.ScreeningResponse;
import com.goldencinema.backend.service.ScreeningService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/screenings")
public class AdminScreeningController {

    private final ScreeningService screeningService;

    public AdminScreeningController(ScreeningService screeningService) {
        this.screeningService = screeningService;
    }

    @GetMapping
    public List<ScreeningResponse> getAllScreenings() {
        return screeningService.getAllScreenings();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ScreeningResponse createScreening(@RequestBody CreateScreeningRequest request) {
        return screeningService.createScreening(request);
    }

    @PutMapping("/{id}")
    public ScreeningResponse updateScreening(@PathVariable Long id,
                                             @RequestBody CreateScreeningRequest request) {
        return screeningService.updateScreening(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelScreening(@PathVariable Long id) {
        screeningService.cancelScreening(id);
        return ResponseEntity.noContent().build();
    }
}
