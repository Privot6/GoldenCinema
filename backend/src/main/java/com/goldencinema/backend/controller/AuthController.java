package com.goldencinema.backend.controller;

import com.goldencinema.backend.dto.LoginRequest;
import com.goldencinema.backend.dto.LoginResponse;
import com.goldencinema.backend.dto.RegisterRequest;
import com.goldencinema.backend.entity.User;
import com.goldencinema.backend.repository.UserRepository;
import com.goldencinema.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;


/**
 * Kontroler obsługujący uwierzytelnianie użytkowników.
 * Udostępnia endpointy rejestracji, logowania i pobierania danych zalogowanego użytkownika.
 */
@RestController
@RequestMapping({"/auth", "/api/auth"})
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    /** Endpoint diagnostyczny sprawdzający dostępność modułu auth. */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Auth endpoint działa");
    }

    /**
     * Rejestruje nowego użytkownika i zwraca token JWT.
     *
     * @param request dane rejestracyjne (imię, nazwisko, email, hasło, telefon)
     * @return token JWT i typ tokenu
     */
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    /**
     * Loguje użytkownika na podstawie emaila i hasła.
     *
     * @param request dane logowania (email, hasło)
     * @return token JWT i typ tokenu
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Zwraca podstawowe dane aktualnie zalogowanego użytkownika.
     *
     * @param authentication kontekst bezpieczeństwa Spring Security
     * @return mapa z polami: email, firstName, lastName
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return ResponseEntity.ok(Map.of(
                "email", user.getEmail(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName()
        ));
    }
}