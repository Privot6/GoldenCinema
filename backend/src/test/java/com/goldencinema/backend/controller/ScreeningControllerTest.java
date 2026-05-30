package com.goldencinema.backend.controller;

import com.goldencinema.backend.BaseIntegrationTest;
import com.goldencinema.backend.entity.CinemaHall;
import com.goldencinema.backend.entity.Movie;
import com.goldencinema.backend.entity.Screening;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// GET /api/screenings/{id}/seats wymaga uwierzytelnienia (anyRequest().authenticated() w SecurityConfig)

class ScreeningControllerTest extends BaseIntegrationTest {

    @Test
    void getUpcomingScreenings_publiczny_zwracaListe() throws Exception {
        Movie movie = createMovie();
        CinemaHall hall = createHall();
        createScreening(movie, hall);

        mockMvc.perform(get("/api/screenings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getSeatAvailability_istniejacySeans_zwracaRzedy() throws Exception {
        String userEmail = uniqueEmail();
        createUser(userEmail, "USER");
        Movie movie = createMovie();
        CinemaHall hall = createHall();
        Screening screening = createScreening(movie, hall);

        mockMvc.perform(get("/api/screenings/" + screening.getId() + "/seats")
                        .header("Authorization", bearerToken(userEmail, "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].rowLabel").exists());
    }

    @Test
    void getSeatAvailability_nieistniejacySeans_zwraca404() throws Exception {
        String userEmail = uniqueEmail();
        createUser(userEmail, "USER");

        mockMvc.perform(get("/api/screenings/999999/seats")
                        .header("Authorization", bearerToken(userEmail, "USER")))
                .andExpect(status().isNotFound());
    }

    @Test
    void getScreeningsByMovieId_zwracaListe() throws Exception {
        Movie movie = createMovie();
        CinemaHall hall = createHall();
        createScreening(movie, hall);

        mockMvc.perform(get("/api/movies/" + movie.getId() + "/screenings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getScreeningsByMovieId_filmBezSeansow_zwracaPustaListe() throws Exception {
        Movie movie = createMovie();

        mockMvc.perform(get("/api/movies/" + movie.getId() + "/screenings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}
