package com.goldencinema.backend.controller;

import com.goldencinema.backend.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest extends BaseIntegrationTest {

    @Test
    void register_sukces_zwracaToken() throws Exception {
        String email = uniqueEmail();
        String body = """
                {"firstName":"Jan","lastName":"Kowalski","email":"%s","password":"Test1234!"}
                """.formatted(email);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void register_duplikatEmail_zwraca409() throws Exception {
        String email = uniqueEmail();
        createUser(email, "USER");

        String body = """
                {"firstName":"Jan","lastName":"Kowalski","email":"%s","password":"Test1234!"}
                """.formatted(email);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void register_brakImienia_zwraca400() throws Exception {
        String body = """
                {"lastName":"Kowalski","email":"brak@test.com","password":"Test1234!"}
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_prawidloweHaslo_zwracaToken() throws Exception {
        String email = uniqueEmail();
        createUser(email, "USER");

        String body = """
                {"email":"%s","password":"Test1234!"}
                """.formatted(email);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_zleHaslo_zwraca401() throws Exception {
        String email = uniqueEmail();
        createUser(email, "USER");

        String body = """
                {"email":"%s","password":"ZleHaslo!"}
                """.formatted(email);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_nieistniejacyEmail_zwraca401() throws Exception {
        String body = """
                {"email":"nieistnieje@test.com","password":"Test1234!"}
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_zalogowany_zwracaDaneUzytkownika() throws Exception {
        String email = uniqueEmail();
        createUser(email, "USER");

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", bearerToken(email, "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void me_bezTokenu_zwraca401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
