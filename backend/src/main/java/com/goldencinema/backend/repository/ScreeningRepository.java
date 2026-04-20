package com.goldencinema.backend.repository;

import com.goldencinema.backend.entity.Screening;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScreeningRepository extends JpaRepository<Screening, Long> {

    @Query("SELECT s FROM Screening s JOIN FETCH s.movie JOIN FETCH s.hall WHERE s.startTime > :from ORDER BY s.startTime ASC")
    List<Screening> findUpcomingScreenings(@Param("from") LocalDateTime from);
}
