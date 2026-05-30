package com.goldencinema.backend.controller;

import com.goldencinema.backend.BaseIntegrationTest;
import com.goldencinema.backend.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class EmployeeControllerTest extends BaseIntegrationTest {

    @Test
    void getReservations_jakoPracownik_zwracaListe() throws Exception {
        String empEmail = uniqueEmail();
        createUser(empEmail, "EMPLOYEE");

        mockMvc.perform(get("/api/employee/reservations")
                        .header("Authorization", bearerToken(empEmail, "EMPLOYEE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getReservations_bezRoli_zwraca403() throws Exception {
        String userEmail = uniqueEmail();
        createUser(userEmail, "USER");

        mockMvc.perform(get("/api/employee/reservations")
                        .header("Authorization", bearerToken(userEmail, "USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getReservations_bezTokenu_zwraca401() throws Exception {
        mockMvc.perform(get("/api/employee/reservations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void verifyReservation_istniejacyKod_zwracaWynik() throws Exception {
        String empEmail = uniqueEmail();
        createUser(empEmail, "EMPLOYEE");

        String userEmail = uniqueEmail();
        User user = createUser(userEmail, "USER");

        Movie movie = createMovie();
        CinemaHall hall = createHall();
        Screening screening = createScreening(movie, hall);

        String code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setScreening(screening);
        reservation.setReservationCode(code);
        reservation.setStatus(ReservationStatus.OCZEKUJACA);
        reservation.setTotalPrice(new BigDecimal("25.00"));
        reservation.setCreatedAt(LocalDateTime.now());
        reservation.setUpdatedAt(LocalDateTime.now());
        reservationRepository.save(reservation);

        mockMvc.perform(get("/api/employee/reservations/verify/" + code)
                        .header("Authorization", bearerToken(empEmail, "EMPLOYEE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationCode").value(code))
                .andExpect(jsonPath("$.isValid").value(true));
    }

    @Test
    void verifyReservation_nieistniejacyKod_zwraca404() throws Exception {
        String empEmail = uniqueEmail();
        createUser(empEmail, "EMPLOYEE");

        mockMvc.perform(get("/api/employee/reservations/verify/ZZZZZZZZ")
                        .header("Authorization", bearerToken(empEmail, "EMPLOYEE")))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateStatus_dozwolonePrzejscie_zwraca200() throws Exception {
        String empEmail = uniqueEmail();
        createUser(empEmail, "EMPLOYEE");

        String userEmail = uniqueEmail();
        User user = createUser(userEmail, "USER");

        Movie movie = createMovie();
        CinemaHall hall = createHall();
        Screening screening = createScreening(movie, hall);

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setScreening(screening);
        reservation.setReservationCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        reservation.setStatus(ReservationStatus.OCZEKUJACA);
        reservation.setTotalPrice(new BigDecimal("25.00"));
        reservation.setCreatedAt(LocalDateTime.now());
        reservation.setUpdatedAt(LocalDateTime.now());
        reservation = reservationRepository.save(reservation);

        String body = """
                {"status":"POTWIERDZONA"}
                """;

        mockMvc.perform(patch("/api/employee/reservations/" + reservation.getId() + "/status")
                        .header("Authorization", bearerToken(empEmail, "EMPLOYEE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("POTWIERDZONA"));
    }

    @Test
    void updateStatus_niedozwolonePrzejscie_zwraca422() throws Exception {
        String empEmail = uniqueEmail();
        createUser(empEmail, "EMPLOYEE");

        String userEmail = uniqueEmail();
        User user = createUser(userEmail, "USER");

        Movie movie = createMovie();
        CinemaHall hall = createHall();
        Screening screening = createScreening(movie, hall);

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setScreening(screening);
        reservation.setReservationCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        reservation.setStatus(ReservationStatus.ANULOWANA);
        reservation.setTotalPrice(new BigDecimal("25.00"));
        reservation.setCreatedAt(LocalDateTime.now());
        reservation.setUpdatedAt(LocalDateTime.now());
        reservation = reservationRepository.save(reservation);

        String body = """
                {"status":"POTWIERDZONA"}
                """;

        mockMvc.perform(patch("/api/employee/reservations/" + reservation.getId() + "/status")
                        .header("Authorization", bearerToken(empEmail, "EMPLOYEE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableEntity());
    }
}
