package com.goldencinema.backend.service;

import com.goldencinema.backend.dto.HallDto;
import com.goldencinema.backend.dto.MovieDto;
import com.goldencinema.backend.dto.ScreeningResponse;
import com.goldencinema.backend.entity.CinemaHall;
import com.goldencinema.backend.entity.Movie;
import com.goldencinema.backend.entity.Screening;
import com.goldencinema.backend.entity.ScreeningStatus;
import com.goldencinema.backend.repository.ScreeningRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScreeningService {

    private final ScreeningRepository screeningRepository;

    public ScreeningService(ScreeningRepository screeningRepository) {
        this.screeningRepository = screeningRepository;
    }

    public List<ScreeningResponse> getUpcomingScreenings() {
        return screeningRepository.findUpcomingScreenings(
                        LocalDateTime.now(),
                        ScreeningStatus.ZAPLANOWANY
                )
                .stream()
                .map(this::mapToScreeningResponse)
                .toList();
    }

    public List<ScreeningResponse> getUpcomingScreeningsByMovieId(Long movieId) {
        return screeningRepository.findUpcomingScreeningsByMovieId(
                        movieId,
                        LocalDateTime.now(),
                        ScreeningStatus.ZAPLANOWANY
                )
                .stream()
                .map(this::mapToScreeningResponse)
                .toList();
    }

    private ScreeningResponse mapToScreeningResponse(Screening screening) {
        return new ScreeningResponse(
                screening.getId(),
                screening.getStartTime(),
                screening.getEndTime(),
                screening.getBasePrice(),
                mapToMovieDto(screening.getMovie()),
                mapToHallDto(screening.getHall())
        );
    }

    private MovieDto mapToMovieDto(Movie movie) {
        return new MovieDto(
                movie.getId(),
                movie.getTitle(),
                movie.getDurationMinutes(),
                movie.getGenre(),
                movie.getPosterUrl()
        );
    }

    private HallDto mapToHallDto(CinemaHall hall) {
        return new HallDto(
                hall.getId(),
                hall.getName()
        );
    }
}