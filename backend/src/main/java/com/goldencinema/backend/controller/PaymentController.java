package com.goldencinema.backend.controller;

import com.goldencinema.backend.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Kontroler obsługujący webhooki Stripe.
 * Endpoint jest wywoływany przez Stripe po zmianie statusu płatności.
 */
@RestController
@RequestMapping("/api/payments/stripe")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Przyjmuje i przetwarza webhook od Stripe (np. potwierdzenie płatności).
     *
     * @param payload   surowe ciało żądania w formacie JSON od Stripe
     * @param sigHeader nagłówek {@code Stripe-Signature} do weryfikacji podpisu
     * @return 200 OK po pomyślnym przetworzeniu
     */
    @PostMapping("/webhook")
    public ResponseEntity<Void> handleStripeWebhook(@RequestBody String payload,
                                                    @RequestHeader("Stripe-Signature") String sigHeader) {
        paymentService.handleWebhook(payload, sigHeader);
        return ResponseEntity.ok().build();
    }
}