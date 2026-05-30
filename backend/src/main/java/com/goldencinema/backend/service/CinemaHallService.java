package com.goldencinema.backend.service;

import com.goldencinema.backend.dto.CinemaHallCreateRequest;
import com.goldencinema.backend.dto.CinemaHallLayoutResponse;
import com.goldencinema.backend.dto.HallDto;
import com.goldencinema.backend.dto.SeatGridItemDto;
import com.goldencinema.backend.entity.CinemaHall;
import com.goldencinema.backend.entity.Seat;
import com.goldencinema.backend.repository.CinemaHallRepository;
import com.goldencinema.backend.repository.SeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serwis zarządzający salami kinowymi i układem miejsc.
 */
@Service
public class CinemaHallService {

    private final CinemaHallRepository hallRepository;
    private final SeatRepository seatRepository;

    public CinemaHallService(CinemaHallRepository hallRepository, SeatRepository seatRepository) {
        this.hallRepository = hallRepository;
        this.seatRepository = seatRepository;
    }

    /**
     * Zwraca listę wszystkich sal kinowych.
     *
     * @return lista sal z id i nazwą
     */
    public List<HallDto> getAllHalls() {
        return hallRepository.findAll().stream()
                .map(h -> new HallDto(h.getId(), h.getName()))
                .toList();
    }

    /**
     * Tworzy nową salę kinową wraz z pełnym układem miejsc.
     *
     * @param request dane sali (nazwa, lista miejsc z pozycjami w siatce)
     * @return utworzona sala z listą miejsc
     */
    @Transactional
    public CinemaHallLayoutResponse createHall(CinemaHallCreateRequest request) {
        CinemaHall hall = new CinemaHall();
        hall.setName(request.getName());
        hall.setIsActive(true);
        hall.setCreatedAt(LocalDateTime.now());
        hall.setUpdatedAt(LocalDateTime.now());
        // rowsCount / seatsPerRow are derived from the grid – store maximums
        int maxRow = request.getSeats().stream().mapToInt(SeatGridItemDto::getGridRow).max().orElse(0);
        int maxCol = request.getSeats().stream().mapToInt(SeatGridItemDto::getGridCol).max().orElse(0);
        hall.setRowsCount(maxRow + 1);
        hall.setSeatsPerRow(maxCol + 1);
        hall = hallRepository.save(hall);

        for (SeatGridItemDto dto : request.getSeats()) {
            Seat seat = new Seat();
            seat.setHall(hall);
            seat.setGridRow(dto.getGridRow());
            seat.setGridCol(dto.getGridCol());
            seat.setRowLabel(dto.getRowLabel());
            seat.setSeatNumber(dto.getSeatNumber());
            seat.setIsActive(true);
            seatRepository.save(seat);
        }

        return buildLayoutResponse(hall);
    }

    /**
     * Zwraca pełny układ miejsc dla wybranej sali.
     *
     * @param id identyfikator sali
     * @return sala z listą aktywnych miejsc
     */
    public CinemaHallLayoutResponse getHallLayout(Long id) {
        CinemaHall hall = hallRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hall not found: " + id));
        return buildLayoutResponse(hall);
    }

    /**
     * Aktualizuje układ miejsc istniejącej sali.
     * Zachowuje ID istniejących miejsc (po rowLabel:seatNumber), by nie zerwać FK rezerwacji.
     *
     * @param id      identyfikator sali
     * @param request nowe dane sali z układem miejsc
     * @return zaktualizowana sala z nowym układem miejsc
     */
    @Transactional
    public CinemaHallLayoutResponse updateHallLayout(Long id, CinemaHallCreateRequest request) {
        CinemaHall hall = hallRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hall not found: " + id));

        hall.setName(request.getName());
        hall.setUpdatedAt(LocalDateTime.now());

        // Index existing seats by (rowLabel:seatNumber) — the actual unique DB key.
        // Reusing existing entity IDs keeps reservation_seats FK references intact.
        List<Seat> existingSeats = seatRepository.findAllByHallId(id);
        Map<String, Seat> byLabel = new HashMap<>();
        for (Seat s : existingSeats) {
            byLabel.put(s.getRowLabel() + ":" + s.getSeatNumber(), s);
        }

        // Deactivate all existing seats; reactivate or create below.
        existingSeats.forEach(s -> s.setIsActive(false));
        seatRepository.saveAll(existingSeats);
        // Flush deactivations to DB before any inserts to avoid unique constraint conflicts.
        seatRepository.flush();

        int maxRow = 0, maxCol = 0;
        for (SeatGridItemDto dto : request.getSeats()) {
            if (dto.getGridRow() > maxRow) maxRow = dto.getGridRow();
            if (dto.getGridCol() > maxCol) maxCol = dto.getGridCol();

            String labelKey = dto.getRowLabel() + ":" + dto.getSeatNumber();
            Seat seat = byLabel.getOrDefault(labelKey, new Seat());
            seat.setHall(hall);
            seat.setGridRow(dto.getGridRow());
            seat.setGridCol(dto.getGridCol());
            seat.setRowLabel(dto.getRowLabel());
            seat.setSeatNumber(dto.getSeatNumber());
            seat.setIsActive(true);
            seatRepository.save(seat);
        }

        hall.setRowsCount(maxRow + 1);
        hall.setSeatsPerRow(maxCol + 1);
        hall = hallRepository.save(hall);

        return buildLayoutResponse(hall);
    }

    /**
     * Usuwa salę kinową o podanym identyfikatorze.
     *
     * @param id identyfikator sali do usunięcia
     */
    public void deleteHall(Long id) {
        if (!hallRepository.existsById(id)) {
            throw new RuntimeException("Hall not found: " + id);
        }
        hallRepository.deleteById(id);
    }

    private CinemaHallLayoutResponse buildLayoutResponse(CinemaHall hall) {
        List<SeatGridItemDto> seats = seatRepository.findAllByHallId(hall.getId()).stream()
                .filter(s -> Boolean.TRUE.equals(s.getIsActive()))
                .map(s -> new SeatGridItemDto(s.getGridRow(), s.getGridCol(), s.getRowLabel(), s.getSeatNumber()))
                .toList();
        return new CinemaHallLayoutResponse(hall.getId(), hall.getName(), seats);
    }
}
