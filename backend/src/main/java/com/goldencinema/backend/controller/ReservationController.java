package com.goldencinema.backend.controller;

import com.goldencinema.backend.dto.CheckoutUrlResponse;
import com.goldencinema.backend.dto.CreateReservationRequest;
import com.goldencinema.backend.dto.ReservationPaymentResponse;
import com.goldencinema.backend.dto.ReservationResponse;
import com.goldencinema.backend.service.ReservationService;
import com.stripe.exception.StripeException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Kontroler zarządzający rezerwacjami biletów.
 * Wymaga uwierzytelnienia — operacje dotyczą zalogowanego użytkownika.
 */
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    /**
     * Tworzy rezerwację i inicjuje sesję płatności Stripe.
     *
     * @param request        dane rezerwacji (seans, miejsca, typy biletów)
     * @param authentication kontekst zalogowanego użytkownika
     * @return rezerwacja wraz z URL do płatności Stripe
     * @throws StripeException gdy nie uda się utworzyć sesji płatności
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationPaymentResponse createReservation(@RequestBody CreateReservationRequest request, Authentication authentication) throws StripeException {
        return reservationService.createReservationPayment(request, authentication);
    }

    /**
     * Zwraca listę rezerwacji zalogowanego użytkownika.
     *
     * @return lista rezerwacji bieżącego użytkownika
     */
    @GetMapping("/my")
    public List<ReservationResponse> getMyReservations() {
        return reservationService.getMyReservations();
    }

    /**
     * Generuje nowy URL do płatności dla istniejącej rezerwacji.
     *
     * @param id identyfikator rezerwacji
     * @return URL do sesji płatności Stripe
     * @throws StripeException gdy nie uda się utworzyć sesji płatności
     */
    @GetMapping("/{id}/checkout")
    public CheckoutUrlResponse getCheckoutUrl(@PathVariable Long id) throws StripeException {
        return reservationService.createCheckoutUrlForExistingReservation(id);
    }

    /**
     * Anuluje rezerwację przez klienta.
     *
     * @param id             identyfikator rezerwacji
     * @param authentication kontekst zalogowanego użytkownika
     * @return zaktualizowana rezerwacja ze statusem ANULOWANA
     */
    @PatchMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public ReservationResponse cancel(@PathVariable Long id, Authentication authentication) {
        return reservationService.cancelByClient(id, authentication.getName());
    }
}