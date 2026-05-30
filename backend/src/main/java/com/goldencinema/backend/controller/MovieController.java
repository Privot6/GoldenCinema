package com.goldencinema.backend.controller;

import com.goldencinema.backend.dto.CreateMovieRequest;
import com.goldencinema.backend.dto.MovieResponse;
import com.goldencinema.backend.service.MovieService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Kontroler zarządzający filmami w repertuarze kina.
 * Umożliwia tworzenie, pobieranie, aktualizację i usuwanie filmów.
 */
@RestController
@RequestMapping({"/movies", "/api/movies"})
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    /**
     * Tworzy nowy film w systemie.
     *
     * @param request dane nowego filmu (tytuł, opis, czas trwania, gatunek, itp.)
     * @return utworzony film
     */
    @PostMapping
    public ResponseEntity<MovieResponse> createMovie(@Valid @RequestBody CreateMovieRequest request) {
        return ResponseEntity.ok(movieService.createMovie(request));
    }

    /**
     * Zwraca listę wszystkich aktywnych filmów.
     *
     * @return lista filmów
     */
    @GetMapping
    public ResponseEntity<List<MovieResponse>> getAllMovies() {
        return ResponseEntity.ok(movieService.getAllMovies());
    }

    /**
     * Zwraca szczegóły filmu o podanym identyfikatorze.
     *
     * @param id identyfikator filmu
     * @return dane filmu
     */
    @GetMapping("/{id}")
    public ResponseEntity<MovieResponse> getMovieById(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.getMovieById(id));
    }

    /**
     * Aktualizuje dane istniejącego filmu.
     *
     * @param id      identyfikator filmu do zaktualizowania
     * @param request nowe dane filmu
     * @return zaktualizowany film
     */
    @PutMapping("/{id}")
    public ResponseEntity<MovieResponse> updateMovie(@PathVariable Long id,
                                                     @Valid @RequestBody CreateMovieRequest request) {
        return ResponseEntity.ok(movieService.updateMovie(id, request));
    }

    /**
     * Usuwa film o podanym identyfikatorze.
     *
     * @param id identyfikator filmu do usunięcia
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }
}