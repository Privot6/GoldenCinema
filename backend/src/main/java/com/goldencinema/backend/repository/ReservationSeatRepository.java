package com.goldencinema.backend.repository;

import com.goldencinema.backend.entity.ReservationSeat;
import com.goldencinema.backend.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Set;

public interface ReservationSeatRepository extends JpaRepository<ReservationSeat, Long> {

    @Query("""
        SELECT rs.seat.id
        FROM ReservationSeat rs
        WHERE rs.screening.id = :screeningId
          AND rs.reservation.status IN :statuses
    """)
    Set<Long> findReservedSeatIdsByScreeningIdAndReservationStatuses(
            @Param("screeningId") Long screeningId,
            @Param("statuses") Collection<ReservationStatus> statuses
    );
}