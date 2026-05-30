package com.goldencinema.backend.controller;

import com.goldencinema.backend.BaseIntegrationTest;
import com.goldencinema.backend.entity.*;
import com.stripe.model.checkout.Session;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReservationControllerTest extends BaseIntegrationTest {

    @Test
    void createReservation_bezTokenu_zwraca401() throws Exception {
        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createReservation_sukces_zwraca201ZKodem() throws Exception {
        String userEmail = uniqueEmail();
        createUser(userEmail, "USER");
        ensurePriceList(TicketType.NORMALNY, new BigDecimal("1.00"));

        Movie movie = createMovie();
        CinemaHall hall = createHall();
        Screening screening = createScreening(movie, hall);

        List<Seat> seats = seatRepository.findAllByHallIdAndIsActiveTrueOrderByRowLabelAscSeatNumberAsc(hall.getId());
        Long seatId = seats.get(0).getId();

        String body = """
                {"screeningId":%d,"seatIds":[%d],"ticketTypes":["NORMALNY"]}
                """.formatted(screening.getId(), seatId);

        Session mockSession = Mockito.mock(Session.class);
        Mockito.when(mockSession.getUrl()).thenReturn("https://checkout.stripe.com/test");
        Mockito.when(mockSession.getId()).thenReturn("sess_test_123");

        try (MockedStatic<Session> sessionStatic = Mockito.mockStatic(Session.class)) {
            sessionStatic.when(() -> Session.create(Mockito.any(com.stripe.param.checkout.SessionCreateParams.class))).thenReturn(mockSession);

            mockMvc.perform(post("/api/reservations")
                            .header("Authorization", bearerToken(userEmail, "USER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.reservationCode").isNotEmpty())
                    .andExpect(jsonPath("$.status").value("OCZEKUJACA"));
        }
    }

    @Test
    void createReservation_konflikt_zwraca409() throws Exception {
        String user1Email = uniqueEmail();
        String user2Email = uniqueEmail();
        createUser(user1Email, "USER");
        createUser(user2Email, "USER");
        ensurePriceList(TicketType.NORMALNY, new BigDecimal("1.00"));

        Movie movie = createMovie();
        CinemaHall hall = createHall();
        Screening screening = createScreening(movie, hall);

        List<Seat> seats = seatRepository.findAllByHallIdAndIsActiveTrueOrderByRowLabelAscSeatNumberAsc(hall.getId());
        Long seatId = seats.get(0).getId();

        // Rezerwacja pierwszego użytkownika
        Reservation reservation = new Reservation();
        reservation.setUser(userRepository.findByEmail(user1Email).orElseThrow());
        reservation.setScreening(screening);
        reservation.setReservationCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        reservation.setStatus(ReservationStatus.OCZEKUJACA);
        reservation.setTotalPrice(new BigDecimal("25.00"));
        reservation.setCreatedAt(LocalDateTime.now());
        reservation.setUpdatedAt(LocalDateTime.now());
        reservation = reservationRepository.save(reservation);

        ReservationSeat rs = new ReservationSeat();
        rs.setReservation(reservation);
        rs.setSeat(seats.get(0));
        rs.setScreening(screening);
        rs.setTicketType(TicketType.NORMALNY);
        rs.setPrice(new BigDecimal("25.00"));
        reservationSeatRepository.save(rs);

        // Próba rezerwacji tego samego miejsca przez drugiego użytkownika
        String body = """
                {"screeningId":%d,"seatIds":[%d],"ticketTypes":["NORMALNY"]}
                """.formatted(screening.getId(), seatId);

        Session mockSession = Mockito.mock(Session.class);
        try (MockedStatic<Session> sessionStatic = Mockito.mockStatic(Session.class)) {
            sessionStatic.when(() -> Session.create(Mockito.any(com.stripe.param.checkout.SessionCreateParams.class))).thenReturn(mockSession);

            mockMvc.perform(post("/api/reservations")
                            .header("Authorization", bearerToken(user2Email, "USER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isConflict());
        }
    }

    @Test
    void getMyReservations_zalogowany_zwracaListe() throws Exception {
        String userEmail = uniqueEmail();
        createUser(userEmail, "USER");

        mockMvc.perform(get("/api/reservations/my")
                        .header("Authorization", bearerToken(userEmail, "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getMyReservations_bezTokenu_zwraca401() throws Exception {
        mockMvc.perform(get("/api/reservations/my"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cancelReservation_wlasnaRezerwacja_zwraca200() throws Exception {
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

        mockMvc.perform(patch("/api/reservations/" + reservation.getId() + "/cancel")
                        .header("Authorization", bearerToken(userEmail, "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ANULOWANA"));
    }

    @Test
    void cancelReservation_cudzaRezerwacja_zwraca403() throws Exception {
        String owner = uniqueEmail();
        String other = uniqueEmail();
        User ownerUser = createUser(owner, "USER");
        createUser(other, "USER");

        Movie movie = createMovie();
        CinemaHall hall = createHall();
        Screening screening = createScreening(movie, hall);

        Reservation reservation = new Reservation();
        reservation.setUser(ownerUser);
        reservation.setScreening(screening);
        reservation.setReservationCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        reservation.setStatus(ReservationStatus.OCZEKUJACA);
        reservation.setTotalPrice(new BigDecimal("25.00"));
        reservation.setCreatedAt(LocalDateTime.now());
        reservation.setUpdatedAt(LocalDateTime.now());
        reservation = reservationRepository.save(reservation);

        mockMvc.perform(patch("/api/reservations/" + reservation.getId() + "/cancel")
                        .header("Authorization", bearerToken(other, "USER")))
                .andExpect(status().isForbidden());
    }
}
