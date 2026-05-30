package com.goldencinema.backend.controller;

import com.goldencinema.backend.dto.CinemaHallCreateRequest;
import com.goldencinema.backend.dto.CinemaHallLayoutResponse;
import com.goldencinema.backend.dto.HallDto;
import com.goldencinema.backend.service.CinemaHallService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Kontroler zarządzający salami kinowymi.
 * Umożliwia tworzenie, pobieranie układu miejsc, aktualizację i usuwanie sal.
 */
@RestController
@RequestMapping("/api/halls")
public class CinemaHallController {

    private final CinemaHallService cinemaHallService;

    public CinemaHallController(CinemaHallService cinemaHallService) {
        this.cinemaHallService = cinemaHallService;
    }

    /**
     * Zwraca listę wszystkich sal kinowych.
     *
     * @return lista sal z podstawowymi danymi (id, nazwa)
     */
    @GetMapping
    public List<HallDto> getAllHalls() {
        return cinemaHallService.getAllHalls();
    }

    /**
     * Tworzy nową salę kinową wraz z układem miejsc.
     *
     * @param request dane sali (nazwa, liczba rzędów, miejsc w rzędzie)
     * @return utworzona sala z pełnym układem miejsc
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CinemaHallLayoutResponse createHall(@RequestBody CinemaHallCreateRequest request) {
        return cinemaHallService.createHall(request);
    }

    /**
     * Zwraca pełny układ miejsc dla wybranej sali.
     *
     * @param id identyfikator sali
     * @return sala z listą wszystkich miejsc pogrupowanych w rzędy
     */
    @GetMapping("/{id}")
    public CinemaHallLayoutResponse getHallLayout(@PathVariable Long id) {
        return cinemaHallService.getHallLayout(id);
    }

    /**
     * Aktualizuje dane i układ miejsc istniejącej sali.
     *
     * @param id      identyfikator sali do zaktualizowania
     * @param request nowe dane sali
     * @return zaktualizowana sala z nowym układem miejsc
     */
    @PutMapping("/{id}")
    public CinemaHallLayoutResponse updateHallLayout(@PathVariable Long id,
                                                     @RequestBody CinemaHallCreateRequest request) {
        return cinemaHallService.updateHallLayout(id, request);
    }

    /**
     * Usuwa salę kinową o podanym identyfikatorze.
     *
     * @param id identyfikator sali do usunięcia
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHall(@PathVariable Long id) {
        cinemaHallService.deleteHall(id);
        return ResponseEntity.noContent().build();
    }
}
