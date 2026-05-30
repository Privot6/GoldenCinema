package com.goldencinema.backend.service;

import com.goldencinema.backend.entity.*;
import com.goldencinema.backend.repository.ReservationRepository;
import com.goldencinema.backend.repository.ReservationStatusHistoryRepository;
import com.goldencinema.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationStatusHistoryRepository historyRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Reservation reservation;
    private User employee;

    @BeforeEach
    void setUp() {
        employee = new User();
        employee.setEmail("emp@test.com");
        employee.setRoles(Set.of());

        Movie movie = new Movie();
        movie.setTitle("Test");

        CinemaHall hall = new CinemaHall();
        hall.setName("Sala 1");

        Screening screening = new Screening();
        screening.setMovie(movie);
        screening.setHall(hall);
        screening.setStartTime(LocalDateTime.now().plusHours(1));

        User user = new User();
        user.setFirstName("Jan");
        user.setLastName("Kowalski");

        reservation = new Reservation();
        org.springframework.test.util.ReflectionTestUtils.setField(reservation, "id", 10L);
        reservation.setUser(user);
        reservation.setScreening(screening);
        reservation.setReservationCode("ABCD1234");
        reservation.setTotalPrice(new BigDecimal("25.00"));
        reservation.setCreatedAt(LocalDateTime.now());
        reservation.setUpdatedAt(LocalDateTime.now());
        reservation.setReservationSeats(new ArrayList<>());
    }

    // ── Dozwolone przejścia ───────────────────────────────────────────────────

    @Test
    void updateStatus_oczekujacaNaPotwierdzona_sukces() {
        reservation.setStatus(ReservationStatus.OCZEKUJACA);
        when(reservationRepository.findById(10L)).thenReturn(Optional.of(reservation));
        when(userRepository.findByEmail("emp@test.com")).thenReturn(Optional.of(employee));
        when(reservationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = employeeService.updateStatus(10L, ReservationStatus.POTWIERDZONA, "emp@test.com");

        assertThat(result.status()).isEqualTo(ReservationStatus.POTWIERDZONA);
        verify(historyRepository).save(any(ReservationStatusHistory.class));
    }

    @Test
    void updateStatus_oczekujacaNaAnulowana_sukces() {
        reservation.setStatus(ReservationStatus.OCZEKUJACA);
        when(reservationRepository.findById(10L)).thenReturn(Optional.of(reservation));
        when(userRepository.findByEmail("emp@test.com")).thenReturn(Optional.of(employee));
        when(reservationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = employeeService.updateStatus(10L, ReservationStatus.ANULOWANA, "emp@test.com");

        assertThat(result.status()).isEqualTo(ReservationStatus.ANULOWANA);
    }

    @Test
    void updateStatus_potwierdzonaNaAnulowana_sukces() {
        reservation.setStatus(ReservationStatus.POTWIERDZONA);
        when(reservationRepository.findById(10L)).thenReturn(Optional.of(reservation));
        when(userRepository.findByEmail("emp@test.com")).thenReturn(Optional.of(employee));
        when(reservationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = employeeService.updateStatus(10L, ReservationStatus.ANULOWANA, "emp@test.com");

        assertThat(result.status()).isEqualTo(ReservationStatus.ANULOWANA);
    }

    // ── Niedozwolone przejścia ────────────────────────────────────────────────

    @Test
    void updateStatus_potwierdzonaNaOczekujaca_rzuca422() {
        reservation.setStatus(ReservationStatus.POTWIERDZONA);
        when(reservationRepository.findById(10L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() ->
                employeeService.updateStatus(10L, ReservationStatus.OCZEKUJACA, "emp@test.com"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("422");
    }

    @Test
    void updateStatus_anulowanaNaCokolwiek_rzuca422() {
        reservation.setStatus(ReservationStatus.ANULOWANA);
        when(reservationRepository.findById(10L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() ->
                employeeService.updateStatus(10L, ReservationStatus.POTWIERDZONA, "emp@test.com"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("422");
    }

    @Test
    void updateStatus_wygaslaNaCokolwiek_rzuca422() {
        reservation.setStatus(ReservationStatus.WYGASLA);
        when(reservationRepository.findById(10L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() ->
                employeeService.updateStatus(10L, ReservationStatus.POTWIERDZONA, "emp@test.com"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("422");
    }

    @Test
    void updateStatus_nieistniejacaRezerwacja_rzuca404() {
        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                employeeService.updateStatus(99L, ReservationStatus.POTWIERDZONA, "emp@test.com"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    // ── Historia zmian ────────────────────────────────────────────────────────

    @Test
    void updateStatus_dozwolonePrzejscie_zapisujeHistorie() {
        reservation.setStatus(ReservationStatus.OCZEKUJACA);
        when(reservationRepository.findById(10L)).thenReturn(Optional.of(reservation));
        when(userRepository.findByEmail("emp@test.com")).thenReturn(Optional.of(employee));
        when(reservationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        employeeService.updateStatus(10L, ReservationStatus.POTWIERDZONA, "emp@test.com");

        verify(historyRepository, times(1)).save(argThat(h ->
                h.getOldStatus() == ReservationStatus.OCZEKUJACA &&
                h.getNewStatus() == ReservationStatus.POTWIERDZONA
        ));
    }
}
