package com.goldencinema.backend.repository;

import com.goldencinema.backend.entity.Reservation;
import com.goldencinema.backend.entity.ReservationStatus;
import com.goldencinema.backend.entity.Screening;
import com.goldencinema.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findAllByUser(User user);

    List<Reservation> findAllByScreening(Screening screening);

    Optional<Reservation> findByReservationCode(String reservationCode);

    @Query("""
        SELECT DISTINCT r
        FROM Reservation r
        JOIN FETCH r.screening s
        JOIN FETCH s.movie
        JOIN FETCH s.hall
        WHERE r.user.id = :userId
        ORDER BY r.createdAt DESC
    """)
    List<Reservation> findAllByUserIdWithScreening(@Param("userId") Long userId);

    @Query("""
        SELECT DISTINCT r
        FROM Reservation r
        JOIN FETCH r.user
        JOIN FETCH r.screening s
        JOIN FETCH s.movie
        JOIN FETCH s.hall
        ORDER BY r.createdAt DESC
    """)
    List<Reservation> findAllWithEagerLoad();

    @Query("SELECT COALESCE(SUM(r.totalPrice), 0) FROM Reservation r WHERE r.status = :status AND r.createdAt >= :from")
    BigDecimal sumTotalPrice(@Param("status") ReservationStatus status, @Param("from") LocalDateTime from);
}