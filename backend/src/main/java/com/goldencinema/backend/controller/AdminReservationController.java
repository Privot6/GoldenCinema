package com.goldencinema.backend.controller;

import com.goldencinema.backend.dto.AdminReservationDto;
import com.goldencinema.backend.dto.PagedResponse;
import com.goldencinema.backend.dto.UpdateReservationStatusRequest;
import com.goldencinema.backend.service.AdminReservationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Kontroler panelu admina do zarządzania rezerwacjami.
 * Umożliwia przeglądanie wszystkich rezerwacji i zmianę ich statusów.
 */
@RestController
@RequestMapping("/api/admin/reservations")
public class AdminReservationController {

    private final AdminReservationService adminReservationService;

    public AdminReservationController(AdminReservationService adminReservationService) {
        this.adminReservationService = adminReservationService;
    }

    /**
     * Zwraca stronicowaną listę wszystkich rezerwacji w systemie.
     *
     * @param page numer strony (od 0)
     * @param size liczba elementów na stronie (domyślnie 20)
     * @return strona rezerwacji z metadanymi paginacji
     */
    @GetMapping
    public PagedResponse<AdminReservationDto> getAllReservations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return adminReservationService.getAllReservationsPaged(page, size);
    }

    /**
     * Zmienia status wybranej rezerwacji przez administratora.
     *
     * @param id             identyfikator rezerwacji
     * @param request        nowy status rezerwacji
     * @param authentication kontekst zalogowanego administratora
     * @return zaktualizowana rezerwacja
     */
    @PutMapping("/{id}/status")
    public AdminReservationDto updateStatus(@PathVariable Long id,
                                            @RequestBody UpdateReservationStatusRequest request,
                                            Authentication authentication) {
        return adminReservationService.updateStatus(id, request.status(), authentication.getName());
    }
}
