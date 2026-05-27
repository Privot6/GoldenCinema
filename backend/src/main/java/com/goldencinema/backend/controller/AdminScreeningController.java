package com.goldencinema.backend.controller;

import com.goldencinema.backend.dto.CreateScreeningRequest;
import com.goldencinema.backend.dto.PagedResponse;
import com.goldencinema.backend.dto.ScreeningResponse;
import com.goldencinema.backend.service.ScreeningService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/screenings")
public class AdminScreeningController {

    private final ScreeningService screeningService;

    public AdminScreeningController(ScreeningService screeningService) {
        this.screeningService = screeningService;
    }

    @GetMapping
    public PagedResponse<ScreeningResponse> getAllScreenings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return screeningService.getAllScreeningsPaged(page, size);
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
