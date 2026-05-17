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
import java.util.List;

@Service
public class CinemaHallService {

    private final CinemaHallRepository hallRepository;
    private final SeatRepository seatRepository;

    public CinemaHallService(CinemaHallRepository hallRepository, SeatRepository seatRepository) {
        this.hallRepository = hallRepository;
        this.seatRepository = seatRepository;
    }

    public List<HallDto> getAllHalls() {
        return hallRepository.findAll().stream()
                .map(h -> new HallDto(h.getId(), h.getName()))
                .toList();
    }

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

    public CinemaHallLayoutResponse getHallLayout(Long id) {
        CinemaHall hall = hallRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hall not found: " + id));
        return buildLayoutResponse(hall);
    }

    @Transactional
    public CinemaHallLayoutResponse updateHallLayout(Long id, CinemaHallCreateRequest request) {
        CinemaHall hall = hallRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hall not found: " + id));

        hall.setName(request.getName());
        hall.setUpdatedAt(LocalDateTime.now());

        // remove all existing seats via orphanRemoval
        List<Seat> existing = seatRepository.findAllByHallId(id);
        seatRepository.deleteAll(existing);
        seatRepository.flush();

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

    public void deleteHall(Long id) {
        if (!hallRepository.existsById(id)) {
            throw new RuntimeException("Hall not found: " + id);
        }
        hallRepository.deleteById(id);
    }

    private CinemaHallLayoutResponse buildLayoutResponse(CinemaHall hall) {
        List<SeatGridItemDto> seats = seatRepository.findAllByHallId(hall.getId()).stream()
                .map(s -> new SeatGridItemDto(s.getGridRow(), s.getGridCol(), s.getRowLabel(), s.getSeatNumber()))
                .toList();
        return new CinemaHallLayoutResponse(hall.getId(), hall.getName(), seats);
    }
}
