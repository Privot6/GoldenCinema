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

class AdminControllerTest extends BaseIntegrationTest {

    // ── Reservations ─────────────────────────────────────────────────────────

    @Test
    void getAllReservations_jakoAdmin_zwracaStronowanie() throws Exception {
        String adminEmail = uniqueEmail();
        createUser(adminEmail, "ADMIN");

        mockMvc.perform(get("/api/admin/reservations")
                        .header("Authorization", bearerToken(adminEmail, "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber());
    }

    @Test
    void getAllReservations_jakoUser_zwraca403() throws Exception {
        String userEmail = uniqueEmail();
        createUser(userEmail, "USER");

        mockMvc.perform(get("/api/admin/reservations")
                        .header("Authorization", bearerToken(userEmail, "USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateReservationStatus_admin_zwraca200() throws Exception {
        String adminEmail = uniqueEmail();
        createUser(adminEmail, "ADMIN");
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

        mockMvc.perform(put("/api/admin/reservations/" + reservation.getId() + "/status")
                        .header("Authorization", bearerToken(adminEmail, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("POTWIERDZONA"));
    }

    // ── Users ─────────────────────────────────────────────────────────────────

    @Test
    void getAllUsers_jakoAdmin_zwracaStronowanie() throws Exception {
        String adminEmail = uniqueEmail();
        createUser(adminEmail, "ADMIN");

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", bearerToken(adminEmail, "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void createUser_jakoAdmin_zwraca201() throws Exception {
        String adminEmail = uniqueEmail();
        createUser(adminEmail, "ADMIN");
        String newEmail = uniqueEmail();

        String body = """
                {"firstName":"Nowy","lastName":"User","email":"%s","password":"Test1234!","role":"USER"}
                """.formatted(newEmail);

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", bearerToken(adminEmail, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(newEmail));
    }

    @Test
    void updateUser_jakoAdmin_zwraca200() throws Exception {
        String adminEmail = uniqueEmail();
        createUser(adminEmail, "ADMIN");
        String userEmail = uniqueEmail();
        User user = createUser(userEmail, "USER");

        String body = """
                {"firstName":"Zmienione","isActive":true}
                """;

        mockMvc.perform(put("/api/admin/users/" + user.getId())
                        .header("Authorization", bearerToken(adminEmail, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Zmienione"));
    }

    @Test
    void deleteUser_innyUzytkownik_zwraca204() throws Exception {
        String adminEmail = uniqueEmail();
        createUser(adminEmail, "ADMIN");
        String userEmail = uniqueEmail();
        User user = createUser(userEmail, "USER");

        mockMvc.perform(delete("/api/admin/users/" + user.getId())
                        .header("Authorization", bearerToken(adminEmail, "ADMIN")))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_wlasneKonto_zwraca409() throws Exception {
        String adminEmail = uniqueEmail();
        User admin = createUser(adminEmail, "ADMIN");

        mockMvc.perform(delete("/api/admin/users/" + admin.getId())
                        .header("Authorization", bearerToken(adminEmail, "ADMIN")))
                .andExpect(status().isConflict());
    }

    // ── Stats ─────────────────────────────────────────────────────────────────

    @Test
    void getStats_jakoAdmin_zwraca200() throws Exception {
        String adminEmail = uniqueEmail();
        createUser(adminEmail, "ADMIN");

        mockMvc.perform(get("/api/admin/stats")
                        .header("Authorization", bearerToken(adminEmail, "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").isNumber())
                .andExpect(jsonPath("$.totalHalls").isNumber());
    }

    @Test
    void getStats_bezTokenu_zwraca401() throws Exception {
        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isUnauthorized());
    }
}
