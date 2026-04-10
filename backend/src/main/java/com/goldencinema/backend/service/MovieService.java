package com.goldencinema.backend.service;

import com.goldencinema.backend.dto.CreateMovieRequest;
import com.goldencinema.backend.dto.MovieResponse;
import com.goldencinema.backend.entity.Movie;
import com.goldencinema.backend.exception.MovieNotFoundException;
import com.goldencinema.backend.repository.MovieRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MovieService {

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

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

    public List<MovieResponse> getAllMovies() {
        return movieRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public MovieResponse getMovieById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException("Film nie został znaleziony"));

        return mapToResponse(movie);
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