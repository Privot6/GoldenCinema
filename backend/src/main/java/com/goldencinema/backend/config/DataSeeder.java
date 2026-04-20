package com.goldencinema.backend.config;

import com.goldencinema.backend.entity.*;
import com.goldencinema.backend.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Component
@Profile("dev")
public class DataSeeder implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    private final RoleRepository roleRepository;
    private final PriceListRepository priceListRepository;
    private final UserRepository userRepository;
    private final CinemaHallRepository cinemaHallRepository;
    private final SeatRepository seatRepository;
    private final MovieRepository movieRepository;
    private final ScreeningRepository screeningRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(RoleRepository roleRepository,
                      PriceListRepository priceListRepository,
                      UserRepository userRepository,
                      CinemaHallRepository cinemaHallRepository,
                      SeatRepository seatRepository,
                      MovieRepository movieRepository,
                      ScreeningRepository screeningRepository,
                      PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.priceListRepository = priceListRepository;
        this.userRepository = userRepository;
        this.cinemaHallRepository = cinemaHallRepository;
        this.seatRepository = seatRepository;
        this.movieRepository = movieRepository;
        this.screeningRepository = screeningRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (roleRepository.count() > 0) {
            return;
        }

        logger.info("Initializing dev database with test data...");

        // 1. Roles
        Role userRole = new Role();
        userRole.setName("USER");
        userRole.setDescription("Klient kina");
        roleRepository.save(userRole);

        Role employeeRole = new Role();
        employeeRole.setName("EMPLOYEE");
        employeeRole.setDescription("Pracownik kina");
        roleRepository.save(employeeRole);

        Role adminRole = new Role();
        adminRole.setName("ADMIN");
        adminRole.setDescription("Administrator systemu");
        roleRepository.save(adminRole);

        // 2. PriceList
        PriceList normal = new PriceList();
        normal.setTicketType(TicketType.NORMALNY);
        normal.setPriceMultiplier(new BigDecimal("1.00"));
        normal.setDescription("Bilet normalny");
        normal.setIsActive(true);
        priceListRepository.save(normal);

        PriceList ulgowy = new PriceList();
        ulgowy.setTicketType(TicketType.ULGOWY);
        ulgowy.setPriceMultiplier(new BigDecimal("0.70"));
        ulgowy.setDescription("Bilet ulgowy");
        ulgowy.setIsActive(true);
        priceListRepository.save(ulgowy);

        // 3. Users
        String password = passwordEncoder.encode("Test1234!");
        LocalDateTime now = LocalDateTime.now();

        User user = new User();
        user.setFirstName("Jan");
        user.setLastName("Kowalski");
        user.setEmail("user@test.com");
        user.setPhone("123456789");
        user.setPasswordHash(password);
        user.setIsActive(true);
        user.setRoles(Set.of(userRole));
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userRepository.save(user);

        User employee = new User();
        employee.setFirstName("Piotr");
        employee.setLastName("Pracownik");
        employee.setEmail("employee@test.com");
        employee.setPhone("987654321");
        employee.setPasswordHash(password);
        employee.setIsActive(true);
        employee.setRoles(Set.of(employeeRole));
        employee.setCreatedAt(now);
        employee.setUpdatedAt(now);
        userRepository.save(employee);

        User admin = new User();
        admin.setFirstName("Adam");
        admin.setLastName("Admin");
        admin.setEmail("admin@test.com");
        admin.setPhone("111222333");
        admin.setPasswordHash(password);
        admin.setIsActive(true);
        admin.setRoles(Set.of(adminRole));
        admin.setCreatedAt(now);
        admin.setUpdatedAt(now);
        userRepository.save(admin);

        // 4. Cinema Halls & Seats
        CinemaHall hall1 = new CinemaHall();
        hall1.setName("Sala Główna");
        hall1.setRowsCount(8);
        hall1.setSeatsPerRow(10);
        hall1.setIsActive(true);
        hall1.setCreatedAt(now);
        hall1.setUpdatedAt(now);
        hall1 = cinemaHallRepository.save(hall1);

        for (int i = 1; i <= 8; i++) {
            String rowLabel = String.valueOf((char) ('A' + i - 1));
            for (int s = 1; s <= 10; s++) {
                Seat seat = new Seat();
                seat.setHall(hall1);
                seat.setRowLabel(rowLabel);
                seat.setSeatNumber(s);
                seat.setIsActive(true);
                seatRepository.save(seat);
            }
        }

        CinemaHall hall2 = new CinemaHall();
        hall2.setName("Sala Mała");
        hall2.setRowsCount(5);
        hall2.setSeatsPerRow(8);
        hall2.setIsActive(true);
        hall2.setCreatedAt(now);
        hall2.setUpdatedAt(now);
        hall2 = cinemaHallRepository.save(hall2);

        for (int i = 1; i <= 5; i++) {
            String rowLabel = String.valueOf((char) ('A' + i - 1));
            for (int s = 1; s <= 8; s++) {
                Seat seat = new Seat();
                seat.setHall(hall2);
                seat.setRowLabel(rowLabel);
                seat.setSeatNumber(s);
                seat.setIsActive(true);
                seatRepository.save(seat);
            }
        }

        // 5. Movies
        Movie m1 = createMovie("Diuna: Część Druga", "Epicka kontynuacja adaptacji słynnej powieści.", 166, "PG-13", "Angielski", "Polski", "Sci-Fi", "https://example.com/diuna2.jpg", now);
        Movie m2 = createMovie("Deadpool & Wolverine", "Superbohaterowie w akcji.", 120, "R", "Angielski", "Polski", "Akcja / Komedia", "https://example.com/deadpool.jpg", now);
        Movie m3 = createMovie("Kung Fu Panda 4", "Kolejna część przygód Po.", 94, "G", "Polski Dubbing", null, "Animacja / Familijny", "https://example.com/panda.jpg", now);
        Movie m4 = createMovie("The Batman", "Mroczny rycerz wraca do Gotham.", 175, "PG-13", "Angielski", "Polski", "Thriller / Akcja", "https://example.com/batman.jpg", now);
        Movie m5 = createMovie("Oppenheimer", "Historia twórcy bomby atomowej.", 180, "R", "Angielski", "Polski", "Dramat / Historyczny", "https://example.com/oppenheimer.jpg", now);
        
        movieRepository.saveAll(List.of(m1, m2, m3, m4, m5));

        // 6. Screenings
        for (int n = 1; n <= 7; n++) {
            createScreening(m1, hall1, now.plusDays(n).withHour(15).withMinute(0).withSecond(0), now.plusDays(n).withHour(17).withMinute(46).withSecond(0), new BigDecimal("30.00"));
            createScreening(m1, hall1, now.plusDays(n).withHour(19).withMinute(0).withSecond(0), now.plusDays(n).withHour(21).withMinute(46).withSecond(0), new BigDecimal("35.00"));

            createScreening(m2, hall2, now.plusDays(n).withHour(15).withMinute(0).withSecond(0), now.plusDays(n).withHour(17).withMinute(0).withSecond(0), new BigDecimal("25.00"));
            createScreening(m2, hall2, now.plusDays(n).withHour(19).withMinute(0).withSecond(0), now.plusDays(n).withHour(21).withMinute(0).withSecond(0), new BigDecimal("30.00"));

            createScreening(m3, hall1, now.plusDays(n).withHour(13).withMinute(0).withSecond(0), now.plusDays(n).withHour(14).withMinute(34).withSecond(0), new BigDecimal("20.00"));

            createScreening(m4, hall2, now.plusDays(n).withHour(21).withMinute(30).withSecond(0), now.plusDays(n).plusDays(1).withHour(0).withMinute(25).withSecond(0), new BigDecimal("35.00"));

            createScreening(m5, hall1, now.plusDays(n).withHour(10).withMinute(0).withSecond(0), now.plusDays(n).withHour(13).withMinute(0).withSecond(0), new BigDecimal("25.00"));
        }
        
    }

    private Movie createMovie(String title, String desc, int dur, String rating, String lang, String sub, String genre, String url, LocalDateTime now) {
        Movie m = new Movie();
        m.setTitle(title);
        m.setDescription(desc);
        m.setDurationMinutes(dur);
        m.setAgeRating(rating);
        m.setLanguage(lang);
        m.setSubtitles(sub);
        m.setGenre(genre);
        m.setPosterUrl(url);
        m.setIsActive(true);
        m.setCreatedAt(now);
        m.setUpdatedAt(now);
        return m;
    }

    private void createScreening(Movie m, CinemaHall h, LocalDateTime start, LocalDateTime end, BigDecimal price) {
        Screening s = new Screening();
        s.setMovie(m);
        s.setHall(h);
        s.setStartTime(start);
        s.setEndTime(end);
        s.setBasePrice(price);
        s.setStatus(ScreeningStatus.ZAPLANOWANY);
        s.setCreatedAt(LocalDateTime.now());
        s.setUpdatedAt(LocalDateTime.now());
        screeningRepository.save(s);
    }
}
