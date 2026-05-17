package com.goldencinema.backend.repository;

import com.goldencinema.backend.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findAllByHallIdAndIsActiveTrueOrderByRowLabelAscSeatNumberAsc(Long hallId);

    List<Seat> findAllByHallId(Long hallId);
}