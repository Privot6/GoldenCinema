package com.goldencinema.backend.controller;

import com.goldencinema.backend.dto.DailyRevenueDto;
import com.goldencinema.backend.dto.StatsResponse;
import com.goldencinema.backend.entity.ReservationStatus;
import com.goldencinema.backend.entity.ScreeningStatus;
import com.goldencinema.backend.repository.CinemaHallRepository;
import com.goldencinema.backend.repository.ReservationRepository;
import com.goldencinema.backend.repository.ScreeningRepository;
import com.goldencinema.backend.repository.UserRepository;
import com.goldencinema.backend.service.AdminReservationService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Kontroler panelu admina dostarczający statystyki kina.
 * Zwraca zagregowane dane o użytkownikach, seansach i przychodach.
 */
@RestController
@RequestMapping("/api/admin/stats")
public class AdminStatsController {

    private final UserRepository userRepository;
    private final ScreeningRepository screeningRepository;
    private final CinemaHallRepository cinemaHallRepository;
    private final ReservationRepository reservationRepository;
    private final AdminReservationService adminReservationService;

    public AdminStatsController(UserRepository userRepository,
                                ScreeningRepository screeningRepository,
                                CinemaHallRepository cinemaHallRepository,
                                ReservationRepository reservationRepository,
                                AdminReservationService adminReservationService) {
        this.userRepository = userRepository;
        this.screeningRepository = screeningRepository;
        this.cinemaHallRepository = cinemaHallRepository;
        this.reservationRepository = reservationRepository;
        this.adminReservationService = adminReservationService;
    }

    /**
     * Zwraca zagregowane statystyki kina: liczby użytkowników, seansów i przychody bieżącego miesiąca.
     *
     * @return obiekt ze statystykami (użytkownicy, seanse dziś/w tygodniu, sale, przychód miesięczny)
     */
    @Transactional(readOnly = true)
    @GetMapping
    public StatsResponse getStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dayStart = LocalDate.now().atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);
        LocalDateTime weekEnd = now.plusDays(7);
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        return new StatsResponse(
                userRepository.count(),
                screeningRepository.countTodayScreenings(dayStart, dayEnd, ScreeningStatus.ZAPLANOWANY),
                screeningRepository.countUpcomingScreenings(now, weekEnd, ScreeningStatus.ZAPLANOWANY),
                cinemaHallRepository.count(),
                reservationRepository.sumTotalPrice(ReservationStatus.POTWIERDZONA, monthStart)
        );
    }

    @Transactional(readOnly = true)
    @GetMapping("/revenue-chart")
    public List<DailyRevenueDto> getRevenueChart() {
        LocalDateTime from = LocalDate.now().minusDays(6).atStartOfDay();
        return adminReservationService.getDailyRevenue(from);
    }
}
