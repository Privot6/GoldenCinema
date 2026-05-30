package com.goldencinema.backend.controller;

import com.goldencinema.backend.dto.EmployeeReservationDto;
import com.goldencinema.backend.dto.ReservationVerificationDto;
import com.goldencinema.backend.dto.UpdateReservationStatusRequest;
import com.goldencinema.backend.service.EmployeeService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Kontroler obsługujący funkcje pracownika kina.
 * Umożliwia podgląd rezerwacji na dany seans, weryfikację kodów QR i zmianę statusów.
 */
@RestController
@RequestMapping("/api/employee")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * Zwraca rezerwacje, opcjonalnie filtrowane po sansie.
     *
     * @param screeningId opcjonalny identyfikator seansu; jeśli null, zwraca wszystkie
     * @return lista rezerwacji z danymi klientów i miejscami
     */
    @GetMapping("/reservations")
    public List<EmployeeReservationDto> getReservations(
            @RequestParam(required = false) Long screeningId) {
        return employeeService.getReservations(screeningId);
    }

    /**
     * Weryfikuje rezerwację na podstawie kodu z QR biletu.
     *
     * @param code unikalny kod rezerwacji (np. z kodu QR na bilecie)
     * @return wynik weryfikacji — czy rezerwacja jest ważna i szczegóły seansu
     */
    @GetMapping("/reservations/verify/{code}")
    public ReservationVerificationDto verifyReservation(@PathVariable String code) {
        return employeeService.verifyByCode(code);
    }

    /**
     * Zmienia status rezerwacji (np. na POTWIERDZONA po wejściu).
     *
     * @param id             identyfikator rezerwacji
     * @param request        nowy status rezerwacji
     * @param authentication kontekst zalogowanego pracownika
     * @return zaktualizowana rezerwacja
     */
    @PatchMapping("/reservations/{id}/status")
    public EmployeeReservationDto updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateReservationStatusRequest request,
            Authentication authentication) {
        return employeeService.updateStatus(id, request.status(), authentication.getName());
    }
}