package com.goldencinema.backend.service;

import com.goldencinema.backend.dto.CreateScreeningRequest;
import com.goldencinema.backend.dto.HallDto;
import com.goldencinema.backend.dto.MovieDto;
import com.goldencinema.backend.dto.PagedResponse;
import com.goldencinema.backend.dto.ScreeningResponse;
import com.goldencinema.backend.dto.SeatAvailabilityDto;
import com.goldencinema.backend.dto.SeatRowDto;
import com.goldencinema.backend.entity.CinemaHall;
import com.goldencinema.backend.entity.Movie;
import com.goldencinema.backend.entity.ReservationStatus;
import com.goldencinema.backend.entity.Screening;
import com.goldencinema.backend.entity.ScreeningStatus;
import com.goldencinema.backend.entity.Seat;
import com.goldencinema.backend.repository.CinemaHallRepository;
import com.goldencinema.backend.repository.MovieRepository;
import com.goldencinema.backend.repository.ReservationSeatRepository;
import com.goldencinema.backend.repository.ScreeningRepository;
import com.goldencinema.backend.repository.SeatRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Serwis zarządzający seansami kinowymi i dostępnością miejsc.
 */
@Service
public class ScreeningService {

    private final ScreeningRepository screeningRepository;
    private final SeatRepository seatRepository;
    private final ReservationSeatRepository reservationSeatRepository;
    private final MovieRepository movieRepository;
    private final CinemaHallRepository cinemaHallRepository;

    public ScreeningService(ScreeningRepository screeningRepository,
                            SeatRepository seatRepository,
                            ReservationSeatRepository reservationSeatRepository,
                            MovieRepository movieRepository,
                            CinemaHallRepository cinemaHallRepository) {
        this.screeningRepository = screeningRepository;
        this.seatRepository = seatRepository;
        this.reservationSeatRepository = reservationSeatRepository;
        this.movieRepository = movieRepository;
        this.cinemaHallRepository = cinemaHallRepository;
    }

    /**
     * Zwraca listę zaplanowanych seansów od bieżącego momentu.
     *
     * @return lista nadchodzących seansów
     */
    public List<ScreeningResponse> getUpcomingScreenings() {
        return screeningRepository.findUpcomingScreenings(
                        LocalDateTime.now(),
                        ScreeningStatus.ZAPLANOWANY
                )
                .stream()
                .map(this::mapToScreeningResponse)
                .toList();
    }

    /**
     * Zwraca nadchodzące seanse dla konkretnego filmu.
     *
     * @param movieId identyfikator filmu
     * @return lista seansów danego filmu
     */
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

    /**
     * Zwraca układ miejsc z informacją o dostępności dla danego seansu.
     * Miejsca z aktywną rezerwacją (OCZEKUJACA lub POTWIERDZONA) są oznaczone jako zajęte.
     *
     * @param screeningId identyfikator seansu
     * @return lista rzędów z miejscami i ich dostępnością
     */
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
                    !reservedSeatIds.contains(seat.getId()),
                    seat.getGridCol()
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

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ScreeningResponse getScreeningById(Long id) {
        Screening s = screeningRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seans nie istnieje"));
        return mapToScreeningResponse(s);
    }

    public List<ScreeningResponse> getAllScreenings() {
        return screeningRepository.findAllWithMovieAndHall().stream()
                .map(this::mapToScreeningResponse)
                .toList();
    }

    /**
     * Zwraca stronicowaną listę wszystkich seansów, posortowanych malejąco po czasie rozpoczęcia.
     *
     * @param page numer strony (od 0)
     * @param size liczba elementów na stronie
     * @return strona seansów z metadanymi paginacji
     */
    public PagedResponse<ScreeningResponse> getAllScreeningsPaged(int page, int size) {
        Page<Screening> result = screeningRepository.findAllWithMovieAndHall(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startTime")));
        return new PagedResponse<>(
                result.getContent().stream().map(this::mapToScreeningResponse).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isLast()
        );
    }

    /**
     * Tworzy nowy seans w repertuarze.
     *
     * @param request dane seansu (film, sala, czas, cena bazowa)
     * @return utworzony seans jako DTO
     */
    public ScreeningResponse createScreening(CreateScreeningRequest request) {
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Movie not found"));
        CinemaHall hall = cinemaHallRepository.findById(request.getHallId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hall not found"));
        Screening s = new Screening();
        s.setMovie(movie);
        s.setHall(hall);
        s.setStartTime(request.getStartTime());
        s.setEndTime(request.getEndTime());
        s.setBasePrice(request.getBasePrice());
        s.setStatus(ScreeningStatus.ZAPLANOWANY);
        s.setCreatedAt(java.time.LocalDateTime.now());
        s.setUpdatedAt(java.time.LocalDateTime.now());
        return mapToScreeningResponse(screeningRepository.save(s));
    }

    /**
     * Aktualizuje dane istniejącego seansu.
     *
     * @param id      identyfikator seansu
     * @param request nowe dane seansu
     * @return zaktualizowany seans jako DTO
     */
    public ScreeningResponse updateScreening(Long id, CreateScreeningRequest request) {
        Screening s = screeningRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Screening not found"));
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Movie not found"));
        CinemaHall hall = cinemaHallRepository.findById(request.getHallId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hall not found"));
        s.setMovie(movie);
        s.setHall(hall);
        s.setStartTime(request.getStartTime());
        s.setEndTime(request.getEndTime());
        s.setBasePrice(request.getBasePrice());
        s.setUpdatedAt(java.time.LocalDateTime.now());
        return mapToScreeningResponse(screeningRepository.save(s));
    }

    /**
     * Anuluje seans — ustawia status na ANULOWANY.
     *
     * @param id identyfikator seansu do anulowania
     */
    public void cancelScreening(Long id) {
        Screening s = screeningRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Screening not found"));
        s.setStatus(ScreeningStatus.ANULOWANY);
        s.setUpdatedAt(java.time.LocalDateTime.now());
        screeningRepository.save(s);
    }

    private ScreeningResponse mapToScreeningResponse(Screening screening) {
        return new ScreeningResponse(
                screening.getId(),
                screening.getStartTime(),
                screening.getEndTime(),
                screening.getBasePrice(),
                screening.getStatus(),
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