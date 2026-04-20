package com.goldencinema.backend.repository;

import com.goldencinema.backend.entity.Reservation;
import com.goldencinema.backend.entity.ReservationStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationStatusHistoryRepository extends JpaRepository<ReservationStatusHistory, Long> {

    List<ReservationStatusHistory> findAllByReservationOrderByChangedAtDesc(Reservation reservation);
}
