package com.goldencinema.backend.service;

import com.goldencinema.backend.dto.CreateReservationRequest;
import com.goldencinema.backend.dto.HallDto;
import com.goldencinema.backend.dto.MovieDto;
import com.goldencinema.backend.dto.ReservationResponse;
import com.goldencinema.backend.dto.ReservedSeatDto;
import com.goldencinema.backend.dto.ScreeningResponse;
import com.goldencinema.backend.entity.PriceList;
import com.goldencinema.backend.entity.Reservation;
import com.goldencinema.backend.entity.ReservationSeat;
import com.goldencinema.backend.entity.ReservationStatus;
import com.goldencinema.backend.entity.Screening;
import com.goldencinema.backend.entity.Seat;
import com.goldencinema.backend.entity.TicketType;
import com.goldencinema.backend.entity.User;
import com.goldencinema.backend.repository.PriceListRepository;
import com.goldencinema.backend.repository.ReservationRepository;
import com.goldencinema.backend.repository.ReservationSeatRepository;
import com.goldencinema.backend.repository.ScreeningRepository;
import com.goldencinema.backend.repository.SeatRepository;
import com.goldencinema.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
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

    public ReservationResponse createReservation(CreateReservationRequest request) {
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

        return mapToReservationResponse(savedReservation);
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