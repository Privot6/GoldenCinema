package com.goldencinema.backend.controller;

import com.goldencinema.backend.dto.CreateReservationRequest;
import com.goldencinema.backend.dto.ReservationResponse;
import com.goldencinema.backend.service.ReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse createReservation(@RequestBody CreateReservationRequest request) {
        return reservationService.createReservation(request);
    }

    @GetMapping("/my")
    public List<ReservationResponse> getMyReservations() {
        return reservationService.getMyReservations();
    }
}