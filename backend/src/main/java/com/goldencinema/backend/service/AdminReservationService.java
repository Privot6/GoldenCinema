package com.goldencinema.backend.service;

import com.goldencinema.backend.dto.AdminReservationDto;
import com.goldencinema.backend.dto.PagedResponse;
import com.goldencinema.backend.entity.Reservation;
import com.goldencinema.backend.entity.ReservationStatus;
import com.goldencinema.backend.entity.User;
import com.goldencinema.backend.repository.ReservationRepository;
import com.goldencinema.backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;

    public AdminReservationService(ReservationRepository reservationRepository,
                                   UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminReservationDto> getAllReservations() {
        return reservationRepository.findAllWithEagerLoad().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public PagedResponse<AdminReservationDto> getAllReservationsPaged(int page, int size) {
        Page<Reservation> result = reservationRepository.findAllWithEagerLoad(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return new PagedResponse<>(
                result.getContent().stream().map(this::toDto).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isLast()
        );
    }

    @Transactional
    public AdminReservationDto updateStatus(Long id, ReservationStatus newStatus, String adminEmail) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found"));

        ReservationStatus current = reservation.getStatus();
        boolean valid = (newStatus == ReservationStatus.POTWIERDZONA && current == ReservationStatus.OCZEKUJACA)
                || (newStatus == ReservationStatus.ANULOWANA
                        && (current == ReservationStatus.OCZEKUJACA || current == ReservationStatus.POTWIERDZONA));

        if (!valid) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Niedozwolone przejście statusu: " + current + " -> " + newStatus);
        }

        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found"));

        if (newStatus == ReservationStatus.POTWIERDZONA) {
            reservation.setConfirmedBy(admin);
        } else {
            reservation.setCancelledBy(admin);
        }

        reservation.setStatus(newStatus);
        reservation.setUpdatedAt(LocalDateTime.now());
        return toDto(reservationRepository.save(reservation));
    }

    private AdminReservationDto toDto(Reservation r) {
        return new AdminReservationDto(
                r.getId(),
                r.getReservationCode(),
                r.getStatus(),
                r.getTotalPrice(),
                r.getCreatedAt(),
                r.getUser().getFirstName(),
                r.getUser().getLastName(),
                r.getUser().getEmail(),
                r.getScreening().getMovie().getTitle(),
                r.getScreening().getHall().getName(),
                r.getScreening().getStartTime()
        );
    }
}
