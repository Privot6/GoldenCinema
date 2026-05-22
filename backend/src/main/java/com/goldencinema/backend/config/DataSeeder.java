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
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Component
@Profile({"dev", "docker"})
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final RoleRepository roleRepository;
    private final PriceListRepository priceListRepository;
    private final UserRepository userRepository;
    private final CinemaHallRepository cinemaHallRepository;
    private final SeatRepository seatRepository;
    private final MovieRepository movieRepository;
    private final ScreeningRepository screeningRepository;
    private final ReservationRepository reservationRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(RoleRepository roleRepository, PriceListRepository priceListRepository,
                      UserRepository userRepository, CinemaHallRepository cinemaHallRepository,
                      SeatRepository seatRepository, MovieRepository movieRepository,
                      ScreeningRepository screeningRepository, ReservationRepository reservationRepository,
                      PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.priceListRepository = priceListRepository;
        this.userRepository = userRepository;
        this.cinemaHallRepository = cinemaHallRepository;
        this.seatRepository = seatRepository;
        this.movieRepository = movieRepository;
        this.screeningRepository = screeningRepository;
        this.reservationRepository = reservationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) return;
        log.info("Empty database detected – seeding...");
        seedAll();
        log.info("Seeding complete. Screenings: {}, Reservations: {}",
                screeningRepository.count(), reservationRepository.count());
    }

    // ── top-level ────────────────────────────────────────────────────────────

    private void seedAll() {
        Role userRole     = ensureRole("USER",     "Klient kina");
        Role employeeRole = ensureRole("EMPLOYEE", "Pracownik kina");
        Role adminRole    = ensureRole("ADMIN",    "Administrator systemu");

        seedPriceList();

        List<User> regularUsers = seedUsers(userRole, employeeRole, adminRole);
        List<CinemaHall> halls  = seedHalls();
        List<Movie>      movies = seedMovies();
        List<Screening>  past   = seedScreenings(movies, halls);
        seedReservations(regularUsers, past);
    }

    // ── roles & prices ───────────────────────────────────────────────────────

    private Role ensureRole(String name, String desc) {
        return roleRepository.findByName(name).orElseGet(() -> {
            Role r = new Role(); r.setName(name); r.setDescription(desc);
            return roleRepository.save(r);
        });
    }

    private void seedPriceList() {
        if (priceListRepository.count() > 0) return;
        savePriceEntry(TicketType.NORMALNY, "1.00", "Bilet normalny");
        savePriceEntry(TicketType.ULGOWY,   "0.70", "Bilet ulgowy");
    }

    private void savePriceEntry(TicketType type, String mult, String desc) {
        PriceList p = new PriceList();
        p.setTicketType(type);
        p.setPriceMultiplier(new BigDecimal(mult));
        p.setDescription(desc);
        p.setIsActive(true);
        priceListRepository.save(p);
    }

    // ── users ────────────────────────────────────────────────────────────────

    private List<User> seedUsers(Role userRole, Role employeeRole, Role adminRole) {
        String pw = passwordEncoder.encode("Test1234!");
        LocalDateTime now = LocalDateTime.now();

        createUser("Adam",       "Admin",       "admin@test.com",       "111222333", pw, Set.of(adminRole),    now);
        createUser("Piotr",      "Pracownik",   "employee@test.com",    "987654321", pw, Set.of(employeeRole), now.minusDays(300));
        createUser("Anna",       "Pracownik",   "employee2@test.com",   "888777666", pw, Set.of(employeeRole), now.minusDays(280));

        String[][] clients = {
            {"Jan",        "Kowalski",     "user@test.com",    "123456789"},
            {"Maria",      "Nowak",        "user2@test.com",   "234567890"},
            {"Tomasz",     "Wiśniewski",   "user3@test.com",   "345678901"},
            {"Agnieszka",  "Zając",        "user4@test.com",   "456789012"},
            {"Robert",     "Szymański",    "user5@test.com",   "567890123"},
            {"Katarzyna",  "Woźniak",      "user6@test.com",   "678901234"},
            {"Marek",      "Kowalczyk",    "user7@test.com",   "789012345"},
            {"Joanna",     "Lewandowska",  "user8@test.com",   "890123456"},
            {"Krzysztof",  "Wójcik",       "user9@test.com",   "901234567"},
            {"Aleksandra", "Kamińska",     "user10@test.com",  "012345678"},
            {"Dawid",      "Jabłoński",    "user11@test.com",  "123123123"},
            {"Monika",     "Pietrzak",     "user12@test.com",  "456456456"},
        };

        List<User> regularUsers = new ArrayList<>();
        for (int i = 0; i < clients.length; i++) {
            String[] c = clients[i];
            regularUsers.add(createUser(c[0], c[1], c[2], c[3], pw, Set.of(userRole), now.minusDays(i * 15L)));
        }
        return regularUsers;
    }

    private User createUser(String first, String last, String email, String phone,
                            String pw, Set<Role> roles, LocalDateTime createdAt) {
        User u = new User();
        u.setFirstName(first); u.setLastName(last); u.setEmail(email); u.setPhone(phone);
        u.setPasswordHash(pw); u.setIsActive(true); u.setRoles(roles);
        u.setCreatedAt(createdAt); u.setUpdatedAt(createdAt);
        return userRepository.save(u);
    }

    // ── halls ────────────────────────────────────────────────────────────────

    private List<CinemaHall> seedHalls() {
        LocalDateTime now = LocalDateTime.now();
        List<CinemaHall> halls = new ArrayList<>();
        halls.add(buildHall("Sala Główna",  8, 10, now));
        halls.add(buildHall("Sala VIP",     5,  8, now));
        halls.add(buildHall("Sala IMAX",   10, 12, now));
        halls.add(buildHall("Sala Mała",    4,  6, now));
        return halls;
    }

    private CinemaHall buildHall(String name, int rows, int seatsPerRow, LocalDateTime now) {
        CinemaHall h = new CinemaHall();
        h.setName(name); h.setRowsCount(rows); h.setSeatsPerRow(seatsPerRow);
        h.setIsActive(true); h.setCreatedAt(now); h.setUpdatedAt(now);
        h = cinemaHallRepository.save(h);
        for (int r = 0; r < rows; r++) {
            String label = String.valueOf((char) ('A' + r));
            for (int s = 1; s <= seatsPerRow; s++) {
                Seat seat = new Seat();
                seat.setHall(h); seat.setRowLabel(label); seat.setSeatNumber(s);
                seat.setIsActive(true);
                seatRepository.save(seat);
            }
        }
        return h;
    }

    // ── movies ───────────────────────────────────────────────────────────────

    private List<Movie> seedMovies() {
        LocalDateTime now = LocalDateTime.now();
        String[][] data = {
            {"Diuna: Część Druga",                       "Epicka kontynuacja adaptacji słynnej powieści Franka Herberta.",   "166", "PG-13", "Angielski",      "Polski",  "Sci-Fi"},
            {"Deadpool & Wolverine",                     "Superbohaterowie w komediowej akcji.",                             "120", "R",     "Angielski",      "Polski",  "Akcja / Komedia"},
            {"Kung Fu Panda 4",                          "Kolejna część przygód niedźwiedzia Po.",                           "94",  "G",     "Polski Dubbing", "",        "Animacja / Familijny"},
            {"The Batman",                               "Mroczny rycerz powraca, by uratować Gotham.",                      "175", "PG-13", "Angielski",      "Polski",  "Thriller / Akcja"},
            {"Oppenheimer",                              "Historia twórcy bomby atomowej i jego moralnych rozterek.",         "180", "R",     "Angielski",      "Polski",  "Dramat / Historyczny"},
            {"Szybcy i Wściekli X",                      "Dominic Toretto i jego rodzina stawiają czoła nowym wrogom.",      "141", "PG-13", "Angielski",      "Polski",  "Akcja"},
            {"Strażnicy Galaktyki 3",                    "Ostatnia misja strażników, by uratować Rocketa.",                  "150", "PG-13", "Angielski",      "Polski",  "Akcja / Sci-Fi"},
            {"Transformers: Przebudzenie Bestii",        "Optimus Prime i nowi sojusznicy w walce o Ziemię.",               "127", "PG-13", "Angielski",      "Polski",  "Akcja / Sci-Fi"},
            {"Indiana Jones i Artefakt Przeznaczenia",   "Ostatnia wielka przygoda doktora Jonesa.",                         "154", "PG-13", "Angielski",      "Polski",  "Przygodowy"},
            {"Elemental",                                "Pixar o żywiołach i miłości ponad różnicami.",                    "101", "PG",    "Polski Dubbing", "",        "Animacja / Familijny"},
            {"Wonka",                                    "Historia młodego, pełnego marzeń Willi Wonki.",                   "116", "PG",    "Angielski",      "Polski",  "Musical / Familijny"},
            {"Napoleon",                                 "Epicki portret życia i upadku Napoleona Bonapartego.",             "158", "R",     "Angielski",      "Polski",  "Dramat Historyczny"},
            {"Wicked",                                   "Zdjęcia z musicalu o czarownicach z Krainy Oz.",                  "160", "PG",    "Angielski",      "Polski",  "Musical"},
            {"Kraven: Łowca",                            "Najbardziej niebezpieczny myśliwy Marvela w akcji.",              "127", "PG-13", "Angielski",      "Polski",  "Akcja / Superbohater"},
            {"Nosferatu",                                "Mroczna, współczesna wersja klasycznego horroru.",                 "132", "R",     "Angielski",      "Polski",  "Horror"},
        };
        List<Movie> movies = new ArrayList<>();
        for (String[] row : data) {
            Movie m = new Movie();
            m.setTitle(row[0]); m.setDescription(row[1]);
            m.setDurationMinutes(Integer.parseInt(row[2]));
            m.setAgeRating(row[3]); m.setLanguage(row[4]);
            m.setSubtitles(row[5].isEmpty() ? null : row[5]);
            m.setGenre(row[6]); m.setPosterUrl(""); m.setIsActive(true);
            m.setCreatedAt(now); m.setUpdatedAt(now);
            movies.add(movieRepository.save(m));
        }
        return movies;
    }

    // ── screenings ───────────────────────────────────────────────────────────
    //  4 halls × 2 slots × 63 days ≈ 504 screenings

    private static final int[][] MORNING_SLOTS = {{10, 0}, {11, 30}, {13, 0}, {10, 30}};
    private static final int[][] EVENING_SLOTS = {{17, 0}, {18, 30}, {20, 0}, {19, 0}};
    private static final BigDecimal[] HALL_PRICES = {
        new BigDecimal("30.00"), new BigDecimal("45.00"),
        new BigDecimal("40.00"), new BigDecimal("25.00")
    };

    private List<Screening> seedScreenings(List<Movie> movies, List<CinemaHall> halls) {
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        Random rng = new Random(42);
        List<Screening> pastScreenings = new ArrayList<>();

        for (int day = -45; day <= 17; day++) {
            LocalDateTime date = now.plusDays(day);
            for (int hi = 0; hi < halls.size(); hi++) {
                CinemaHall hall = halls.get(hi);
                BigDecimal basePrice = HALL_PRICES[hi];

                // morning screening
                Movie m1 = movies.get(rng.nextInt(movies.size()));
                int[] ms = MORNING_SLOTS[hi];
                LocalDateTime mStart = date.withHour(ms[0]).withMinute(ms[1]);
                LocalDateTime mEnd   = mStart.plusMinutes(m1.getDurationMinutes() + 20);

                ScreeningStatus mStatus = day < 0
                        ? (rng.nextInt(20) == 0 ? ScreeningStatus.ANULOWANY : ScreeningStatus.ZAKONCZONY)
                        : ScreeningStatus.ZAPLANOWANY;
                Screening ms1 = saveScreening(m1, hall, mStart, mEnd, basePrice, mStatus, now);
                if (mStatus == ScreeningStatus.ZAKONCZONY) pastScreenings.add(ms1);

                // evening screening
                Movie m2 = movies.get(rng.nextInt(movies.size()));
                int[] es = EVENING_SLOTS[hi];
                LocalDateTime eStart = date.withHour(es[0]).withMinute(es[1]);
                LocalDateTime eEnd   = eStart.plusMinutes(m2.getDurationMinutes() + 20);

                ScreeningStatus eStatus = day < 0
                        ? (rng.nextInt(20) == 0 ? ScreeningStatus.ANULOWANY : ScreeningStatus.ZAKONCZONY)
                        : ScreeningStatus.ZAPLANOWANY;
                Screening es1 = saveScreening(m2, hall, eStart, eEnd,
                        basePrice.add(new BigDecimal("5.00")), eStatus, now);
                if (eStatus == ScreeningStatus.ZAKONCZONY) pastScreenings.add(es1);
            }
        }
        return pastScreenings;
    }

    private Screening saveScreening(Movie m, CinemaHall h, LocalDateTime start, LocalDateTime end,
                                    BigDecimal price, ScreeningStatus status, LocalDateTime now) {
        Screening s = new Screening();
        s.setMovie(m); s.setHall(h); s.setStartTime(start); s.setEndTime(end);
        s.setBasePrice(price); s.setStatus(status); s.setCreatedAt(now); s.setUpdatedAt(now);
        return screeningRepository.save(s);
    }

    // ── reservations ─────────────────────────────────────────────────────────
    //  1–3 per past screening, mix of statuses, realistic totalPrice

    private void seedReservations(List<User> users, List<Screening> pastScreenings) {
        Random rng = new Random(42);
        ReservationStatus[] statuses = {
            ReservationStatus.POTWIERDZONA, ReservationStatus.POTWIERDZONA,
            ReservationStatus.POTWIERDZONA, ReservationStatus.ANULOWANA,
            ReservationStatus.OCZEKUJACA
        };
        BigDecimal[] mults = {
            BigDecimal.ONE, BigDecimal.ONE, new BigDecimal("0.70")
        };

        for (Screening s : pastScreenings) {
            int count = 1 + rng.nextInt(3);
            for (int i = 0; i < count; i++) {
                User user   = users.get(rng.nextInt(users.size()));
                ReservationStatus status = statuses[rng.nextInt(statuses.length)];
                int seats   = 1 + rng.nextInt(4);
                BigDecimal mult = mults[rng.nextInt(mults.length)];
                BigDecimal total = s.getBasePrice()
                        .multiply(mult)
                        .multiply(BigDecimal.valueOf(seats))
                        .setScale(2, RoundingMode.HALF_UP);
                LocalDateTime createdAt = s.getStartTime().minusDays(1 + rng.nextInt(10));

                Reservation r = new Reservation();
                r.setUser(user); r.setScreening(s);
                r.setReservationCode("RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                r.setStatus(status); r.setTotalPrice(total);
                r.setCreatedAt(createdAt); r.setUpdatedAt(createdAt);
                reservationRepository.save(r);
            }
        }
    }
}
