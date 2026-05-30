package com.goldencinema.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
    }

    @Test
    void generateToken_zwracaNieputyToken() {
        String token = jwtService.generateToken("user@test.com", "USER");
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void extractEmail_zwracaPoprawnegoPodmiotu() {
        String token = jwtService.generateToken("user@test.com", "USER");
        assertThat(jwtService.extractEmail(token)).isEqualTo("user@test.com");
    }

    @Test
    void extractRole_zwracaPoprawnaRole() {
        String token = jwtService.generateToken("admin@test.com", "ADMIN");
        assertThat(jwtService.extractRole(token)).isEqualTo("ADMIN");
    }

    @Test
    void isTokenValid_swiezyToken_zwracaTrue() {
        String token = jwtService.generateToken("user@test.com", "USER");
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_popsutyCiag_zwracaFalse() {
        assertThat(jwtService.isTokenValid("to.nie.jest.token")).isFalse();
    }

    @Test
    void isTokenValid_pustyString_zwracaFalse() {
        assertThat(jwtService.isTokenValid("")).isFalse();
    }

    @Test
    void rozneRole_generujaNiezalezneTokeny() {
        String userToken = jwtService.generateToken("user@test.com", "USER");
        String adminToken = jwtService.generateToken("admin@test.com", "ADMIN");

        assertThat(jwtService.extractRole(userToken)).isEqualTo("USER");
        assertThat(jwtService.extractRole(adminToken)).isEqualTo("ADMIN");
        assertThat(userToken).isNotEqualTo(adminToken);
    }

    @Test
    void generowanieTokenu_zachowujePrawnoscEmail() {
        // Tokeny dla różnych emaili mają właściwy email
        String token1 = jwtService.generateToken("user1@test.com", "USER");
        String token2 = jwtService.generateToken("user2@test.com", "USER");

        assertThat(jwtService.extractEmail(token1)).isEqualTo("user1@test.com");
        assertThat(jwtService.extractEmail(token2)).isEqualTo("user2@test.com");
        assertThat(token1).isNotEqualTo(token2);
    }
}
