package com.goldencinema.backend.service;

import com.goldencinema.backend.dto.*;
import com.goldencinema.backend.entity.*;
import com.goldencinema.backend.repository.*;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationSeatRepository reservationSeatRepository;
    private final ScreeningRepository screeningRepository;
    private final SeatRepository seatRepository;
    private final PriceListRepository priceListRepository;
    private final UserRepository userRepository;

    @Value("${stripe.apiKey}")
    private String stripeApiKey;

    @Value("${stripe.successUrl:http://localhost:3000/success}")
    private String stripeSuccessUrl;

    @Value("${stripe.cancelUrl:http://localhost:3000/cancel}")
    private String stripeCancelUrl;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationSeatRepository reservationSeatRepository,
                              ScreeningRepository screeningRepository,
                              SeatRepository seatRepository,
                              PriceListRepository priceListRepository,
                              UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationSeatRepository = reservationSeatRepository;
        this.screeningRepository = screeningRepository;
        this.seatRepository = seatRepository;
        this.priceListRepository = priceListRepository;
        this.userRepository = userRepository;
    }

    public ReservationPaymentResponse createReservationPayment(CreateReservationRequest request,
                                                               Authentication authentication) throws StripeException {
        validateRequest(request);

        User user = getCurrentUser();

        Screening screening = screeningRepository.findById(request.screeningId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Screening not found"));

        List<Seat> seats = seatRepository.findAllById(request.seatIds());

        if (seats.size() != request.seatIds().size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "One or more seats not found");
        }

        Set<Long> reservedSeatIds = reservationSeatRepository.findReservedSeatIdsByScreeningIdAndReservationStatuses(
                request.screeningId(),
                Set.of(ReservationStatus.OCZEKUJACA, ReservationStatus.POTWIERDZONA)
        );

        for (Long seatId : request.seatIds()) {
            if (reservedSeatIds.contains(seatId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Seat " + seatId + " is already reserved");
            }
        }

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setScreening(screening);
        reservation.setReservationCode(generateReservationCode());
        reservation.setStatus(ReservationStatus.OCZEKUJACA);
        reservation.setCreatedAt(LocalDateTime.now());
        reservation.setUpdatedAt(LocalDateTime.now());

        BigDecimal totalPrice = BigDecimal.ZERO;

        for (int i = 0; i < seats.size(); i++) {
            Seat seat = seats.get(i);
            TicketType ticketType = request.ticketTypes().get(i);

            PriceList priceList = priceListRepository.findByTicketType(ticketType)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ticket type"));

            BigDecimal seatPrice = screening.getBasePrice().multiply(priceList.getPriceMultiplier());
            totalPrice = totalPrice.add(seatPrice);

            ReservationSeat reservationSeat = new ReservationSeat();
            reservationSeat.setReservation(reservation);
            reservationSeat.setScreening(screening);
            reservationSeat.setSeat(seat);
            reservationSeat.setTicketType(ticketType);
            reservationSeat.setPrice(seatPrice);

            reservation.getReservationSeats().add(reservationSeat);
        }

        reservation.setTotalPrice(totalPrice);

        Reservation savedReservation = reservationRepository.save(reservation);

        Session session = createStripeSession(savedReservation);

        return new ReservationPaymentResponse(
                savedReservation.getId(),
                savedReservation.getReservationCode(),
                savedReservation.getStatus(),
                savedReservation.getTotalPrice(),
                session.getUrl(),
                session.getId()
        );
    }

    public CheckoutUrlResponse createCheckoutUrlForExistingReservation(Long reservationId) throws StripeException {
        User user = getCurrentUser();

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found"));

        if (!reservation.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your reservation");
        }

        if (reservation.getStatus() != ReservationStatus.OCZEKUJACA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reservation is not pending payment");
        }

        Session session = createStripeSession(reservation);
        return new CheckoutUrlResponse(session.getUrl());
    }

    private Session createStripeSession(Reservation reservation) throws StripeException {
        Stripe.apiKey = stripeApiKey;

        long amountInCents = reservation.getTotalPrice()
                .multiply(new BigDecimal(100))
                .longValue();

        SessionCreateParams.LineItem.PriceData.ProductData product =
                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                        .setName("Rezerwacja " + reservation.getReservationCode() + " - " +
                                reservation.getScreening().getMovie().getTitle())
                        .build();

        SessionCreateParams params = SessionCreateParams.builder()
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setPriceData(
                                SessionCreateParams.LineItem.PriceData.builder()
                                        .setCurrency("pln")
                                        .setUnitAmount(amountInCents)
                                        .setProductData(product)
                                        .build()
                        )
                        .setQuantity(1L)
                        .build())
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(stripeSuccessUrl + "?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(stripeCancelUrl)
                .putMetadata("reservationId", reservation.getId().toString())
                .putMetadata("reservationCode", reservation.getReservationCode())
                .build();

        return Session.create(params);
    }

    public List<ReservationResponse> getMyReservations() {
        User user = getCurrentUser();

        return reservationRepository.findAllByUserIdWithScreening(user.getId())
                .stream()
                .map(this::mapToReservationResponse)
                .toList();
    }

    private void validateRequest(CreateReservationRequest request) {
        if (request.screeningId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "screeningId is required");
        }

        if (request.seatIds() == null || request.seatIds().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "seatIds are required");
        }

        if (request.ticketTypes() == null || request.ticketTypes().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ticketTypes are required");
        }

        if (request.seatIds().size() != request.ticketTypes().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "seatIds and ticketTypes size must match");
        }
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private String generateReservationCode() {
        return UUID.randomUUID().toString()
                .substring(0, 8)
                .toUpperCase();
    }

    private ReservationResponse mapToReservationResponse(Reservation reservation) {
        List<ReservedSeatDto> reservedSeats = reservation.getReservationSeats()
                .stream()
                .map(this::mapToReservedSeatDto)
                .toList();

        return new ReservationResponse(
                reservation.getId(),
                reservation.getReservationCode(),
                reservation.getStatus(),
                reservation.getTotalPrice(),
                mapToScreeningResponse(reservation.getScreening()),
                reservedSeats
        );
    }

    private ReservedSeatDto mapToReservedSeatDto(ReservationSeat reservationSeat) {
        Seat seat = reservationSeat.getSeat();

        return new ReservedSeatDto(
                seat.getId(),
                seat.getRowLabel(),
                seat.getSeatNumber(),
                reservationSeat.getTicketType(),
                reservationSeat.getPrice()
        );
    }

    private ScreeningResponse mapToScreeningResponse(Screening screening) {
        return new ScreeningResponse(
                screening.getId(),
                screening.getStartTime(),
                screening.getEndTime(),
                screening.getBasePrice(),
                screening.getStatus(),
                new MovieDto(
                        screening.getMovie().getId(),
                        screening.getMovie().getTitle(),
                        screening.getMovie().getDurationMinutes(),
                        screening.getMovie().getGenre(),
                        screening.getMovie().getPosterUrl()
                ),
                new HallDto(
                        screening.getHall().getId(),
                        screening.getHall().getName()
                )
        );
    }
}