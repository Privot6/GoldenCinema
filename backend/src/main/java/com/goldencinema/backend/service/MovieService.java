package com.goldencinema.backend.service;

import com.goldencinema.backend.dto.CreateMovieRequest;
import com.goldencinema.backend.dto.MovieResponse;
import com.goldencinema.backend.entity.Movie;
import com.goldencinema.backend.exception.MovieNotFoundException;
import com.goldencinema.backend.repository.MovieRepository;
import com.goldencinema.backend.repository.ScreeningRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Serwis zarządzający filmami w repertuarze kina.
 */
@Service
public class MovieService {

    private final MovieRepository movieRepository;
    private final ScreeningRepository screeningRepository;

    public MovieService(MovieRepository movieRepository, ScreeningRepository screeningRepository) {
        this.movieRepository = movieRepository;
        this.screeningRepository = screeningRepository;
    }

    /**
     * Tworzy nowy film i zapisuje go w bazie danych.
     *
     * @param request dane nowego filmu
     * @return zapisany film jako DTO
     */
    public MovieResponse createMovie(CreateMovieRequest request) {
        LocalDateTime now = LocalDateTime.now();

        Movie movie = new Movie();
        movie.setTitle(request.title());
        movie.setDescription(request.description());
        movie.setDurationMinutes(request.durationMinutes());
        movie.setAgeRating(request.ageRating());
        movie.setLanguage(request.language());
        movie.setSubtitles(request.subtitles());
        movie.setGenre(request.genre());
        movie.setPosterUrl(request.posterUrl());
        movie.setIsActive(true);
        movie.setCreatedAt(now);
        movie.setUpdatedAt(now);

        Movie savedMovie = movieRepository.save(movie);

        return mapToResponse(savedMovie);
    }

    /**
     * Zwraca wszystkie filmy z bazy danych.
     *
     * @return lista filmów jako DTO
     */
    public List<MovieResponse> getAllMovies() {
        return movieRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Zwraca film o podanym identyfikatorze.
     *
     * @param id identyfikator filmu
     * @return film jako DTO
     * @throws MovieNotFoundException gdy film nie istnieje
     */
    public MovieResponse getMovieById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException("Film nie został znaleziony"));
        return mapToResponse(movie);
    }

    /**
     * Aktualizuje dane istniejącego filmu.
     *
     * @param id      identyfikator filmu do zaktualizowania
     * @param request nowe dane filmu
     * @return zaktualizowany film jako DTO
     * @throws MovieNotFoundException gdy film nie istnieje
     */
    public MovieResponse updateMovie(Long id, CreateMovieRequest request) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException("Film nie został znaleziony"));
        movie.setTitle(request.title());
        movie.setDescription(request.description());
        movie.setDurationMinutes(request.durationMinutes());
        movie.setAgeRating(request.ageRating());
        movie.setLanguage(request.language());
        movie.setSubtitles(request.subtitles());
        movie.setGenre(request.genre());
        movie.setPosterUrl(request.posterUrl());
        movie.setUpdatedAt(LocalDateTime.now());
        return mapToResponse(movieRepository.save(movie));
    }

    /**
     * Usuwa film z bazy danych. Nie można usunąć filmu z przypisanymi seansami.
     *
     * @param id identyfikator filmu do usunięcia
     * @throws MovieNotFoundException    gdy film nie istnieje
     * @throws ResponseStatusException   (409 Conflict) gdy film ma przypisane seanse
     */
    public void deleteMovie(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException("Film nie został znaleziony"));
        if (screeningRepository.existsByMovieId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Nie można usunąć filmu, który ma przypisane seanse.");
        }
        movieRepository.delete(movie);
    }

    private MovieResponse mapToResponse(Movie movie) {
        return new MovieResponse(
                movie.getId(),
                movie.getTitle(),
                movie.getDescription(),
                movie.getDurationMinutes(),
                movie.getAgeRating(),
                movie.getLanguage(),
                movie.getSubtitles(),
                movie.getGenre(),
                movie.getPosterUrl(),
                movie.getIsActive(),
                movie.getCreatedAt(),
                movie.getUpdatedAt()
        );
    }
}