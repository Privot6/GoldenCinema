package com.goldencinema.backend.controller;

import com.goldencinema.backend.dto.CinemaHallCreateRequest;
import com.goldencinema.backend.dto.CinemaHallLayoutResponse;
import com.goldencinema.backend.dto.HallDto;
import com.goldencinema.backend.service.CinemaHallService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/halls")
public class CinemaHallController {

    private final CinemaHallService cinemaHallService;

    public CinemaHallController(CinemaHallService cinemaHallService) {
        this.cinemaHallService = cinemaHallService;
    }

    @GetMapping
    public List<HallDto> getAllHalls() {
        return cinemaHallService.getAllHalls();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CinemaHallLayoutResponse createHall(@RequestBody CinemaHallCreateRequest request) {
        return cinemaHallService.createHall(request);
    }

    @GetMapping("/{id}")
    public CinemaHallLayoutResponse getHallLayout(@PathVariable Long id) {
        return cinemaHallService.getHallLayout(id);
    }

    @PutMapping("/{id}")
    public CinemaHallLayoutResponse updateHallLayout(@PathVariable Long id,
                                                     @RequestBody CinemaHallCreateRequest request) {
        return cinemaHallService.updateHallLayout(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHall(@PathVariable Long id) {
        cinemaHallService.deleteHall(id);
        return ResponseEntity.noContent().build();
    }
}
