package com.goldencinema.backend.repository;

import com.goldencinema.backend.entity.Reservation;
import com.goldencinema.backend.entity.Screening;
import com.goldencinema.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findAllByUser(User user);

    List<Reservation> findAllByScreening(Screening screening);

    Optional<Reservation> findByReservationCode(String reservationCode);
}
