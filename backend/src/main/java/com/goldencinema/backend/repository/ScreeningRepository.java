package com.goldencinema.backend.repository;

import com.goldencinema.backend.entity.Screening;
import com.goldencinema.backend.entity.ScreeningStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ScreeningRepository extends JpaRepository<Screening, Long> {

    @Query("""
        SELECT s
        FROM Screening s
        JOIN FETCH s.movie
        JOIN FETCH s.hall
        WHERE s.startTime > :from
          AND s.status = :status
        ORDER BY s.startTime ASC
    """)
    List<Screening> findUpcomingScreenings(
            @Param("from") LocalDateTime from,
            @Param("status") ScreeningStatus status
    );

    @Query("""
        SELECT s
        FROM Screening s
        JOIN FETCH s.movie
        JOIN FETCH s.hall
        WHERE s.movie.id = :movieId
          AND s.startTime > :from
          AND s.status = :status
        ORDER BY s.startTime ASC
    """)
    List<Screening> findUpcomingScreeningsByMovieId(
            @Param("movieId") Long movieId,
            @Param("from") LocalDateTime from,
            @Param("status") ScreeningStatus status
    );
}