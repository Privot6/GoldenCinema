package com.goldencinema.backend.service;

import com.goldencinema.backend.dto.EmployeeReservationDto;
import com.goldencinema.backend.dto.ReservationVerificationDto;
import com.goldencinema.backend.dto.UpdateReservationStatusRequest;
import com.goldencinema.backend.entity.*;
import com.goldencinema.backend.repository.ReservationRepository;
import com.goldencinema.backend.repository.ReservationStatusHistoryRepository;
import com.goldencinema.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Serwis obsługujący funkcje pracownika kina.
 * Zarządza weryfikacją biletów, podglądem rezerwacji i zmianą statusów z walidacją dozwolonych przejść.
 */
@Service
public class EmployeeService {

    private final ReservationRepository reservationRepository;
    private final ReservationStatusHistoryRepository reservationStatusHistoryRepository;
    private final UserRepository userRepository;

    private static final Map<ReservationStatus, Set<ReservationStatus>> ALLOWED_TRANSITIONS = Map.of(
            ReservationStatus.OCZEKUJACA, Set.of(ReservationStatus.POTWIERDZONA, ReservationStatus.ANULOWANA),
            ReservationStatus.POTWIERDZONA, Set.of(ReservationStatus.ANULOWANA),
            ReservationStatus.ANULOWANA, Set.of(),
            ReservationStatus.WYGASLA, Set.of()
    );

    public EmployeeService(ReservationRepository reservationRepository,
                           ReservationStatusHistoryRepository reservationStatusHistoryRepository,
                           UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationStatusHistoryRepository = reservationStatusHistoryRepository;
        this.userRepository = userRepository;
    }

    /**
     * Zwraca rezerwacje, opcjonalnie filtrowane po sansie.
     * Bez filtra zwraca rezerwacje na nadchodzące seanse, posortowane po czasie.
     *
     * @param screeningId opcjonalny identyfikator seansu; jeśli null, zwraca przyszłe seanse
     * @return lista rezerwacji z danymi klientów i seansów
     */
    @Transactional(readOnly = true)
    public List<EmployeeReservationDto> getReservations(Long screeningId) {
        List<Reservation> reservations;

        if (screeningId != null) {
            reservations = reservationRepository.findAllByScreeningId(screeningId);
        } else {
            reservations = reservationRepository.findAllWithEagerLoad().stream()
                    .filter(r -> r.getScreening().getStartTime().isAfter(LocalDateTime.now()))
                    .sorted(Comparator.comparing(r -> r.getScreening().getStartTime()))
                    .toList();
        }

        return reservations.stream()
                .map(this::toEmployeeReservationDto)
                .toList();
    }

    /**
     * Zmienia status rezerwacji z walidacją dozwolonych przejść.
     * Zapisuje historię zmiany statusu.
     *
     * @param id            identyfikator rezerwacji
     * @param newStatus     nowy status rezerwacji
     * @param employeeEmail email pracownika wykonującego zmianę
     * @return zaktualizowana rezerwacja
     * @throws ResponseStatusException (422) gdy przejście statusu jest niedozwolone
     */
    @Transactional
    public EmployeeReservationDto updateStatus(Long id, ReservationStatus newStatus, String employeeEmail) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rezerwacja nie znaleziona"));

        ReservationStatus oldStatus = reservation.getStatus();

        Set<ReservationStatus> allowedNextStatuses = ALLOWED_TRANSITIONS.get(oldStatus);
        if (allowedNextStatuses == null || !allowedNextStatuses.contains(newStatus)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Niedozwolone przejście statusu: " + oldStatus + " -> " + newStatus);
        }

        User employee = userRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pracownik nie znaleziony"));

        if (newStatus == ReservationStatus.POTWIERDZONA) {
            reservation.setConfirmedBy(employee);
        } else if (newStatus == ReservationStatus.ANULOWANA) {
            reservation.setCancelledBy(employee);
        }

        reservation.setStatus(newStatus);
        reservation.setUpdatedAt(LocalDateTime.now());
        reservationRepository.save(reservation);

        ReservationStatusHistory history = new ReservationStatusHistory();
        history.setReservation(reservation);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(employee);
        history.setChangedAt(LocalDateTime.now());
        history.setNote(null);
        reservationStatusHistoryRepository.save(history);

        return toEmployeeReservationDto(reservation);
    }

    /**
     * Weryfikuje rezerwację na podstawie unikalnego kodu (np. z kodu QR na bilecie).
     *
     * @param code unikalny kod rezerwacji
     * @return wynik weryfikacji z informacją czy bilet jest ważny i danymi seansu
     */
    @Transactional(readOnly = true)
    public ReservationVerificationDto verifyByCode(String code) {
        Reservation r = reservationRepository.findByReservationCode(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rezerwacja nie znaleziona"));

        boolean valid = r.getStatus() == ReservationStatus.OCZEKUJACA
                     || r.getStatus() == ReservationStatus.POTWIERDZONA;
        String reason = switch (r.getStatus()) {
            case ANULOWANA -> "Rezerwacja jest anulowana";
            case WYGASLA   -> "Rezerwacja wygasła";
            default        -> null;
        };

        return new ReservationVerificationDto(
                r.getId(),
                r.getReservationCode(),
                r.getStatus(),
                valid,
                reason,
                r.getUser().getFirstName(),
                r.getUser().getLastName(),
                r.getScreening().getMovie().getTitle(),
                r.getScreening().getHall().getName(),
                r.getScreening().getStartTime(),
                r.getTotalPrice(),
                r.getReservationSeats().size()
        );
    }

    private EmployeeReservationDto toEmployeeReservationDto(Reservation r) {
        return new EmployeeReservationDto(
                r.getId(),
                r.getReservationCode(),
                r.getStatus(),
                r.getTotalPrice(),
                r.getCreatedAt(),
                r.getUser().getFirstName(),
                r.getUser().getLastName(),
                r.getScreening().getMovie().getTitle(),
                r.getScreening().getHall().getName(),
                r.getScreening().getStartTime()
        );
    }
}