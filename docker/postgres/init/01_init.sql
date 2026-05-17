CREATE TYPE screening_status_enum AS ENUM ('ZAPLANOWANY', 'ANULOWANY', 'ZAKONCZONY');
CREATE TYPE reservation_status_enum AS ENUM ('OCZEKUJACA', 'POTWIERDZONA', 'ANULOWANA', 'WYGASLA');
CREATE TYPE ticket_type_enum AS ENUM ('NORMALNY', 'ULGOWY');

-- =====================================
-- 1. Roles
-- =====================================

CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- =====================================
-- 2. Users
-- =====================================

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(20),
    password_hash VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- =====================================
-- 3. Movies
-- =====================================

CREATE TABLE movies (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    duration_minutes INT NOT NULL CHECK (duration_minutes > 0),
    age_rating VARCHAR(20),
    language VARCHAR(50),
    subtitles VARCHAR(50),
    genre VARCHAR(100),
    poster_url VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =====================================
-- 4. Cinema halls and seats
-- =====================================

CREATE TABLE cinema_halls (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    rows_count INT NOT NULL CHECK (rows_count > 0),
    seats_per_row INT NOT NULL CHECK (seats_per_row > 0),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE seats (
    id BIGSERIAL PRIMARY KEY,
    hall_id BIGINT NOT NULL,
    row_label VARCHAR(10) NOT NULL,
    seat_number INT NOT NULL CHECK (seat_number > 0),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    grid_row INT,
    grid_col INT,
    CONSTRAINT uq_seat UNIQUE (hall_id, row_label, seat_number),
    CONSTRAINT fk_seat_hall
        FOREIGN KEY (hall_id) REFERENCES cinema_halls(id) ON DELETE CASCADE
);

-- =====================================
-- 5. Screenings
-- =====================================

CREATE TABLE screenings (
    id BIGSERIAL PRIMARY KEY,
    movie_id BIGINT NOT NULL,
    hall_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    base_price NUMERIC(10,2) NOT NULL CHECK (base_price >= 0),
    status screening_status_enum NOT NULL DEFAULT 'ZAPLANOWANY',
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_screening_time CHECK (end_time > start_time),
    CONSTRAINT fk_screening_movie
        FOREIGN KEY (movie_id) REFERENCES movies(id),
    CONSTRAINT fk_screening_hall
        FOREIGN KEY (hall_id) REFERENCES cinema_halls(id),
    CONSTRAINT fk_screening_created_by
        FOREIGN KEY (created_by) REFERENCES users(id)
);

-- =====================================
-- 6. Price list
-- =====================================

CREATE TABLE price_list (
    id BIGSERIAL PRIMARY KEY,
    ticket_type ticket_type_enum NOT NULL UNIQUE,
    price_multiplier NUMERIC(5,2) NOT NULL CHECK (price_multiplier > 0),
    description VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- =====================================
-- 7. Reservations
-- =====================================

CREATE TABLE reservations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    screening_id BIGINT NOT NULL,
    reservation_code VARCHAR(50) NOT NULL UNIQUE,
    status reservation_status_enum NOT NULL DEFAULT 'OCZEKUJACA',
    total_price NUMERIC(10,2) NOT NULL CHECK (total_price >= 0),
    expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confirmed_by BIGINT,
    cancelled_by BIGINT,
    CONSTRAINT fk_reservation_user
        FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_reservation_screening
        FOREIGN KEY (screening_id) REFERENCES screenings(id),
    CONSTRAINT fk_reservation_confirmed_by
        FOREIGN KEY (confirmed_by) REFERENCES users(id),
    CONSTRAINT fk_reservation_cancelled_by
        FOREIGN KEY (cancelled_by) REFERENCES users(id)
);

CREATE TABLE reservation_seats (
    id BIGSERIAL PRIMARY KEY,
    reservation_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    screening_id BIGINT NOT NULL,
    ticket_type ticket_type_enum NOT NULL DEFAULT 'NORMALNY',
    price NUMERIC(10,2) NOT NULL CHECK (price >= 0),
    CONSTRAINT uq_screening_seat UNIQUE (screening_id, seat_id),
    CONSTRAINT fk_reservation_seat_reservation
        FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE CASCADE,
    CONSTRAINT fk_reservation_seat_seat
        FOREIGN KEY (seat_id) REFERENCES seats(id),
    CONSTRAINT fk_reservation_seat_screening
        FOREIGN KEY (screening_id) REFERENCES screenings(id)
);

-- =====================================
-- 8. Reservation status history
-- =====================================

CREATE TABLE reservation_status_history (
    id BIGSERIAL PRIMARY KEY,
    reservation_id BIGINT NOT NULL,
    old_status reservation_status_enum,
    new_status reservation_status_enum NOT NULL,
    changed_by BIGINT,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    note VARCHAR(255),
    CONSTRAINT fk_history_reservation
        FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE CASCADE,
    CONSTRAINT fk_history_changed_by
        FOREIGN KEY (changed_by) REFERENCES users(id)
);

-- =====================================
-- 9. Reports
-- =====================================

CREATE TABLE reports (
    id BIGSERIAL PRIMARY KEY,
    report_type VARCHAR(50) NOT NULL,
    generated_by BIGINT NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_report_user
        FOREIGN KEY (generated_by) REFERENCES users(id)
);

INSERT INTO price_list (ticket_type, price_multiplier, description, is_active) VALUES
('NORMALNY', 1.00, 'Bilet normalny', TRUE),
('ULGOWY', 0.70, 'Bilet ulgowy', TRUE);

INSERT INTO roles (name, description) VALUES
('USER', 'Klient kina'),
('EMPLOYEE', 'Pracownik kina'),
('ADMIN', 'Administrator systemu');