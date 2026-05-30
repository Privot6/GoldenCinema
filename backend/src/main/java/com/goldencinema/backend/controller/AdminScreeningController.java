package com.goldencinema.backend.controller;

import com.goldencinema.backend.dto.CreateScreeningRequest;
import com.goldencinema.backend.dto.PagedResponse;
import com.goldencinema.backend.dto.ScreeningResponse;
import com.goldencinema.backend.service.ScreeningService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Kontroler panelu admina do zarządzania seansami.
 * Umożliwia tworzenie, przeglądanie, edycję i anulowanie seansów.
 */
@RestController
@RequestMapping("/api/admin/screenings")
public class AdminScreeningController {

    private final ScreeningService screeningService;

    public AdminScreeningController(ScreeningService screeningService) {
        this.screeningService = screeningService;
    }

    /**
     * Zwraca stronicowaną listę wszystkich seansów.
     *
     * @param page numer strony (od 0)
     * @param size liczba elementów na stronie (domyślnie 20)
     * @return strona seansów z metadanymi paginacji
     */
    @GetMapping
    public PagedResponse<ScreeningResponse> getAllScreenings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return screeningService.getAllScreeningsPaged(page, size);
    }

    /**
     * Tworzy nowy seans w repertuarze.
     *
     * @param request dane seansu (film, sala, godzina, cena bazowa)
     * @return utworzony seans
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ScreeningResponse createScreening(@RequestBody CreateScreeningRequest request) {
        return screeningService.createScreening(request);
    }

    /**
     * Aktualizuje dane istniejącego seansu.
     *
     * @param id      identyfikator seansu do zaktualizowania
     * @param request nowe dane seansu
     * @return zaktualizowany seans
     */
    @PutMapping("/{id}")
    public ScreeningResponse updateScreening(@PathVariable Long id,
                                             @RequestBody CreateScreeningRequest request) {
        return screeningService.updateScreening(id, request);
    }

    /**
     * Anuluje seans o podanym identyfikatorze.
     *
     * @param id identyfikator seansu do anulowania
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelScreening(@PathVariable Long id) {
        screeningService.cancelScreening(id);
        return ResponseEntity.noContent().build();
    }
}
