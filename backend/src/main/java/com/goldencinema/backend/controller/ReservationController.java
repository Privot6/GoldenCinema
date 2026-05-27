package com.goldencinema.backend.controller;

import com.goldencinema.backend.dto.CreateReservationRequest;
import com.goldencinema.backend.dto.ReservationPaymentResponse;
import com.goldencinema.backend.dto.ReservationResponse;
import com.goldencinema.backend.service.ReservationService;
import com.stripe.exception.StripeException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
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
    public ReservationPaymentResponse createReservation(@RequestBody CreateReservationRequest request, Authentication authentication) throws StripeException {
        return reservationService.createReservationPayment(request, authentication);
    }

    @GetMapping("/my")
    public List<ReservationResponse> getMyReservations() {
        return reservationService.getMyReservations();
    }
}