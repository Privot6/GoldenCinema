package com.goldencinema.backend.repository;

import com.goldencinema.backend.entity.Reservation;
import com.goldencinema.backend.entity.ReservationStatus;
import com.goldencinema.backend.entity.Screening;
import com.goldencinema.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    JOIN FETCH r.user
    JOIN FETCH r.screening s
    JOIN FETCH s.movie
    JOIN FETCH s.hall
    WHERE s.id = :screeningId
    ORDER BY r.createdAt DESC
""")
    List<Reservation> findAllByScreeningId(@Param("screeningId") Long screeningId);
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

    @Query(value = "SELECT r FROM Reservation r JOIN FETCH r.user JOIN FETCH r.screening s JOIN FETCH s.movie JOIN FETCH s.hall",
           countQuery = "SELECT COUNT(r) FROM Reservation r")
    Page<Reservation> findAllWithEagerLoad(Pageable pageable);

    @Query("SELECT COALESCE(SUM(r.totalPrice), 0) FROM Reservation r WHERE r.status = :status AND r.createdAt >= :from")
    BigDecimal sumTotalPrice(@Param("status") ReservationStatus status, @Param("from") LocalDateTime from);

    @Query("""
        SELECT
            r.screening.movie.title,
            COUNT(DISTINCT r.screening.id),
            COUNT(r.id),
            SUM(r.totalPrice)
        FROM Reservation r
        WHERE r.status = :status
          AND r.screening.startTime >= :from
          AND r.screening.startTime < :to
        GROUP BY r.screening.movie.title
        ORDER BY SUM(r.totalPrice) DESC
    """)
    List<Object[]> getWeeklyProfitRawData(
            @Param("status") ReservationStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query(value = """
        SELECT TO_CHAR(r.created_at, 'YYYY-MM-DD') AS date,
               COALESCE(SUM(r.total_price), 0)      AS revenue
        FROM reservations r
        WHERE r.status = 'POTWIERDZONA'
          AND r.created_at >= :from
        GROUP BY TO_CHAR(r.created_at, 'YYYY-MM-DD')
        ORDER BY date ASC
    """, nativeQuery = true)
    List<Object[]> getDailyRevenueRaw(@Param("from") LocalDateTime from);
}