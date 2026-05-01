package com.goldencinema.backend.service;

import com.goldencinema.backend.dto.HallDto;
import com.goldencinema.backend.dto.MovieDto;
import com.goldencinema.backend.dto.ScreeningResponse;
import com.goldencinema.backend.dto.SeatAvailabilityDto;
import com.goldencinema.backend.dto.SeatRowDto;
import com.goldencinema.backend.entity.CinemaHall;
import com.goldencinema.backend.entity.Movie;
import com.goldencinema.backend.entity.ReservationStatus;
import com.goldencinema.backend.entity.Screening;
import com.goldencinema.backend.entity.ScreeningStatus;
import com.goldencinema.backend.entity.Seat;
import com.goldencinema.backend.repository.ReservationSeatRepository;
import com.goldencinema.backend.repository.ScreeningRepository;
import com.goldencinema.backend.repository.SeatRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ScreeningService {

    private final ScreeningRepository screeningRepository;
    private final SeatRepository seatRepository;
    private final ReservationSeatRepository reservationSeatRepository;

    public ScreeningService(ScreeningRepository screeningRepository,
                            SeatRepository seatRepository,
                            ReservationSeatRepository reservationSeatRepository) {
        this.screeningRepository = screeningRepository;
        this.seatRepository = seatRepository;
        this.reservationSeatRepository = reservationSeatRepository;
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

    public List<SeatRowDto> getSeatAvailabilityForScreening(Long screeningId) {
        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Screening not found"));

        Long hallId = screening.getHall().getId();

        List<Seat> seats = seatRepository.findAllByHallIdAndIsActiveTrueOrderByRowLabelAscSeatNumberAsc(hallId);

        Set<Long> reservedSeatIds = reservationSeatRepository.findReservedSeatIdsByScreeningIdAndReservationStatuses(
                screeningId,
                Set.of(ReservationStatus.OCZEKUJACA, ReservationStatus.POTWIERDZONA)
        );

        Map<String, List<SeatAvailabilityDto>> groupedSeats = new LinkedHashMap<>();

        for (Seat seat : seats) {
            SeatAvailabilityDto seatDto = new SeatAvailabilityDto(
                    seat.getId(),
                    seat.getRowLabel(),
                    seat.getSeatNumber(),
                    !reservedSeatIds.contains(seat.getId())
            );

            groupedSeats
                    .computeIfAbsent(seat.getRowLabel(), key -> new ArrayList<>())
                    .add(seatDto);
        }

        return groupedSeats.entrySet()
                .stream()
                .map(entry -> new SeatRowDto(entry.getKey(), entry.getValue()))
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