package com.goldencinema.backend.service;

import com.goldencinema.backend.entity.*;
import com.goldencinema.backend.repository.ReservationRepository;
import com.goldencinema.backend.repository.ReservationStatusHistoryRepository;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class PaymentService {

    private final ReservationRepository reservationRepository;
    private final ReservationStatusHistoryRepository reservationStatusHistoryRepository;

    @Value("${stripe.apiKey}")
    private String stripeApiKey;

    @Value("${stripe.webhookSecret}")
    private String webhookSecret;

    public PaymentService(ReservationRepository reservationRepository,
                          ReservationStatusHistoryRepository reservationStatusHistoryRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationStatusHistoryRepository = reservationStatusHistoryRepository;
    }

    @Transactional
    public void handleWebhook(String payload, String sigHeader) {
        Stripe.apiKey = stripeApiKey;

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Stripe signature");
        }

        switch (event.getType()) {
            case "checkout.session.completed" -> handleCheckoutCompleted(event);
            case "checkout.session.expired" -> handlePaymentExpired(event);
            case "checkout.session.async_payment_failed" -> handlePaymentFailed(event);
            case "payment_intent.payment_failed" -> handlePaymentFailed(event);
        }
    }

    private void handleCheckoutCompleted(Event event) {
        Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
        if (session == null) return;

        String reservationIdStr = session.getMetadata().get("reservationId");
        if (reservationIdStr == null) return;

        Long reservationId = Long.valueOf(reservationIdStr);
        Reservation reservation = reservationRepository.findById(reservationId).orElse(null);
        if (reservation == null) return;

        if (reservation.getStatus() != ReservationStatus.OCZEKUJACA) {
            return;
        }

        ReservationStatus oldStatus = reservation.getStatus();
        reservation.setStatus(ReservationStatus.POTWIERDZONA);
        reservation.setUpdatedAt(LocalDateTime.now());
        reservationRepository.save(reservation);

        ReservationStatusHistory history = new ReservationStatusHistory();
        history.setReservation(reservation);
        history.setOldStatus(oldStatus);
        history.setNewStatus(ReservationStatus.POTWIERDZONA);
        history.setChangedBy(null);
        history.setChangedAt(LocalDateTime.now());
        history.setNote("STRIPE_PAYMENT_SUCCEEDED");
        reservationStatusHistoryRepository.save(history);
    }

    private void handlePaymentExpired(Event event) {
        Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
        if (session == null) return;

        String reservationIdStr = session.getMetadata().get("reservationId");
        if (reservationIdStr == null) return;

        Long reservationId = Long.valueOf(reservationIdStr);
        Reservation reservation = reservationRepository.findById(reservationId).orElse(null);
        if (reservation == null) return;

        if (reservation.getStatus() != ReservationStatus.OCZEKUJACA) {
            return;
        }

        ReservationStatus oldStatus = reservation.getStatus();
        reservation.setStatus(ReservationStatus.ANULOWANA);
        reservation.setUpdatedAt(LocalDateTime.now());
        reservationRepository.save(reservation);

        ReservationStatusHistory history = new ReservationStatusHistory();
        history.setReservation(reservation);
        history.setOldStatus(oldStatus);
        history.setNewStatus(ReservationStatus.ANULOWANA);
        history.setChangedBy(null);
        history.setChangedAt(LocalDateTime.now());
        history.setNote("STRIPE_PAYMENT_EXPIRED");
        reservationStatusHistoryRepository.save(history);
    }

    private void handlePaymentFailed(Event event) {
        Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
        if (session == null) return;

        String reservationIdStr = session.getMetadata().get("reservationId");
        if (reservationIdStr == null) return;

        Long reservationId = Long.valueOf(reservationIdStr);
        Reservation reservation = reservationRepository.findById(reservationId).orElse(null);
        if (reservation == null) return;

        if (reservation.getStatus() != ReservationStatus.OCZEKUJACA) {
            return;
        }

        ReservationStatus oldStatus = reservation.getStatus();
        reservation.setStatus(ReservationStatus.ANULOWANA);
        reservation.setUpdatedAt(LocalDateTime.now());
        reservationRepository.save(reservation);

        ReservationStatusHistory history = new ReservationStatusHistory();
        history.setReservation(reservation);
        history.setOldStatus(oldStatus);
        history.setNewStatus(ReservationStatus.ANULOWANA);
        history.setChangedBy(null);
        history.setChangedAt(LocalDateTime.now());
        history.setNote("STRIPE_PAYMENT_FAILED");
        reservationStatusHistoryRepository.save(history);
    }
}