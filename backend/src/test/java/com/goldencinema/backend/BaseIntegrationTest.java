package com.goldencinema.backend;

import com.goldencinema.backend.entity.*;
import com.goldencinema.backend.repository.*;
import com.goldencinema.backend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @Autowired
    private WebApplicationContext wac;

    protected MockMvc mockMvc;

    @Autowired
    protected JwtService jwtService;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected RoleRepository roleRepository;

    @Autowired
    protected MovieRepository movieRepository;

    @Autowired
    protected CinemaHallRepository cinemaHallRepository;

    @Autowired
    protected ScreeningRepository screeningRepository;

    @Autowired
    protected SeatRepository seatRepository;

    @Autowired
    protected ReservationRepository reservationRepository;

    @Autowired
    protected ReservationSeatRepository reservationSeatRepository;

    @Autowired
    protected PriceListRepository priceListRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUpMockMvc() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    protected String bearerToken(String email, String role) {
        return "Bearer " + jwtService.generateToken(email, role);
    }

    protected User createUser(String email, String roleName) {
        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName(roleName);
                    return roleRepository.save(r);
                });

        User user = new User();
        user.setEmail(email);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPasswordHash(passwordEncoder.encode("Test1234!"));
        user.setIsActive(true);
        user.setRoles(Set.of(role));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    protected Movie createMovie() {
        Movie movie = new Movie();
        movie.setTitle("Test Film " + UUID.randomUUID().toString().substring(0, 8));
        movie.setDescription("Opis testowy");
        movie.setDurationMinutes(120);
        movie.setAgeRating("PG-13");
        movie.setLanguage("Polski");
        movie.setGenre("Akcja");
        movie.setPosterUrl("");
        movie.setIsActive(true);
        movie.setCreatedAt(LocalDateTime.now());
        movie.setUpdatedAt(LocalDateTime.now());
        return movieRepository.save(movie);
    }

    protected CinemaHall createHall() {
        CinemaHall hall = new CinemaHall();
        hall.setName("Sala " + UUID.randomUUID().toString().substring(0, 4));
        hall.setRowsCount(3);
        hall.setSeatsPerRow(5);
        hall.setIsActive(true);
        hall.setCreatedAt(LocalDateTime.now());
        hall.setUpdatedAt(LocalDateTime.now());
        hall = cinemaHallRepository.save(hall);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 5; col++) {
                Seat seat = new Seat();
                seat.setHall(hall);
                seat.setRowLabel(String.valueOf((char) ('A' + row)));
                seat.setSeatNumber(col + 1);
                seat.setGridRow(row);
                seat.setGridCol(col);
                seat.setIsActive(true);
                seatRepository.save(seat);
            }
        }
        return hall;
    }

    protected Screening createScreening(Movie movie, CinemaHall hall) {
        Screening screening = new Screening();
        screening.setMovie(movie);
        screening.setHall(hall);
        screening.setStartTime(LocalDateTime.now().plusHours(1));
        screening.setEndTime(LocalDateTime.now().plusHours(3));
        screening.setBasePrice(new BigDecimal("25.00"));
        screening.setStatus(ScreeningStatus.ZAPLANOWANY);
        screening.setCreatedAt(LocalDateTime.now());
        screening.setUpdatedAt(LocalDateTime.now());
        return screeningRepository.save(screening);
    }

    protected PriceList ensurePriceList(TicketType type, BigDecimal multiplier) {
        return priceListRepository.findByTicketType(type)
                .orElseGet(() -> {
                    PriceList p = new PriceList();
                    p.setTicketType(type);
                    p.setPriceMultiplier(multiplier);
                    p.setDescription(type.name());
                    p.setIsActive(true);
                    return priceListRepository.save(p);
                });
    }

    protected String uniqueEmail() {
        return "test_" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
    }
}
