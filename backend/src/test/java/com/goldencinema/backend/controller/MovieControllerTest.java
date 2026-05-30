package com.goldencinema.backend.controller;

import com.goldencinema.backend.BaseIntegrationTest;
import com.goldencinema.backend.entity.Movie;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MovieControllerTest extends BaseIntegrationTest {

    private static final String MOVIE_JSON = """
            {"title":"Nowy Film","description":"Opis","durationMinutes":120,
             "ageRating":"PG-13","language":"Polski","genre":"Akcja","posterUrl":""}
            """;

    @Test
    void getAllMovies_publiczny_zwracaListe() throws Exception {
        createMovie();

        mockMvc.perform(get("/api/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getMovieById_istniejacy_zwracaFilm() throws Exception {
        Movie movie = createMovie();

        mockMvc.perform(get("/api/movies/" + movie.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(movie.getId()));
    }

    @Test
    void getMovieById_nieistniejacy_zwraca404() throws Exception {
        mockMvc.perform(get("/api/movies/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createMovie_jakoAdmin_zwracaFilm() throws Exception {
        String adminEmail = uniqueEmail();
        createUser(adminEmail, "ADMIN");

        mockMvc.perform(post("/api/movies")
                        .header("Authorization", bearerToken(adminEmail, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MOVIE_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Nowy Film"));
    }

    @Test
    void createMovie_bezTokenu_zwraca401() throws Exception {
        mockMvc.perform(post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MOVIE_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createMovie_jakoUser_zwraca403() throws Exception {
        String userEmail = uniqueEmail();
        createUser(userEmail, "USER");

        mockMvc.perform(post("/api/movies")
                        .header("Authorization", bearerToken(userEmail, "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MOVIE_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateMovie_jakoAdmin_zwracaZaktualizowany() throws Exception {
        Movie movie = createMovie();
        String adminEmail = uniqueEmail();
        createUser(adminEmail, "ADMIN");

        String updateJson = """
                {"title":"Zmieniony Tytuł","description":"Nowy opis","durationMinutes":90,
                 "ageRating":"PG","language":"Angielski","genre":"Dramat","posterUrl":""}
                """;

        mockMvc.perform(put("/api/movies/" + movie.getId())
                        .header("Authorization", bearerToken(adminEmail, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Zmieniony Tytuł"));
    }

    @Test
    void deleteMovie_bezSeansu_zwraca204() throws Exception {
        Movie movie = createMovie();
        String adminEmail = uniqueEmail();
        createUser(adminEmail, "ADMIN");

        mockMvc.perform(delete("/api/movies/" + movie.getId())
                        .header("Authorization", bearerToken(adminEmail, "ADMIN")))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteMovie_zSeansem_zwraca409() throws Exception {
        Movie movie = createMovie();
        createScreening(movie, createHall());

        String adminEmail = uniqueEmail();
        createUser(adminEmail, "ADMIN");

        mockMvc.perform(delete("/api/movies/" + movie.getId())
                        .header("Authorization", bearerToken(adminEmail, "ADMIN")))
                .andExpect(status().isConflict());
    }
}
