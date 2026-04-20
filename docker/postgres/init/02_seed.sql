-- 02_seed.sql

-- 1. Users
INSERT INTO users (first_name, last_name, email, phone, password_hash, is_active) VALUES
('Jan', 'Kowalski', 'user@test.com', '123456789', '$2a$10$gMXXwP.16xNWeR8DbsyB8OepM7N5u988PIt6.o3wN/l4j52k.HItC', true),
('Piotr', 'Pracownik', 'employee@test.com', '987654321', '$2a$10$gMXXwP.16xNWeR8DbsyB8OepM7N5u988PIt6.o3wN/l4j52k.HItC', true),
('Adam', 'Admin', 'admin@test.com', '111222333', '$2a$10$gMXXwP.16xNWeR8DbsyB8OepM7N5u988PIt6.o3wN/l4j52k.HItC', true);

-- 2. User Roles
INSERT INTO user_roles (user_id, role_id) VALUES
((SELECT id FROM users WHERE email = 'user@test.com'), (SELECT id FROM roles WHERE name = 'USER')),
((SELECT id FROM users WHERE email = 'employee@test.com'), (SELECT id FROM roles WHERE name = 'EMPLOYEE')),
((SELECT id FROM users WHERE email = 'admin@test.com'), (SELECT id FROM roles WHERE name = 'ADMIN'));

-- 3. Cinema Halls
INSERT INTO cinema_halls (name, rows_count, seats_per_row, is_active) VALUES
('Sala Główna', 8, 10, true),
('Sala Mała', 5, 8, true);

-- 4. Seats (Generated for Sala Główna: 8 rows, 10 seats per row)
DO $$
DECLARE
    hall_id BIGINT;
    r INT;
    s INT;
    row_label VARCHAR(10);
BEGIN
    SELECT id INTO hall_id FROM cinema_halls WHERE name = 'Sala Główna';
    FOR r IN 1..8 LOOP
        row_label := CHR(64 + r); -- A(65), B(66), ...
        FOR s IN 1..10 LOOP
            INSERT INTO seats (hall_id, row_label, seat_number, is_active) VALUES (hall_id, row_label, s, true);
        END LOOP;
    END LOOP;
END $$;

-- 5. Seats (Generated for Sala Mała: 5 rows, 8 seats per row)
DO $$
DECLARE
    hall_id BIGINT;
    r INT;
    s INT;
    row_label VARCHAR(10);
BEGIN
    SELECT id INTO hall_id FROM cinema_halls WHERE name = 'Sala Mała';
    FOR r IN 1..5 LOOP
        row_label := CHR(64 + r); -- A(65), B(66), ...
        FOR s IN 1..8 LOOP
            INSERT INTO seats (hall_id, row_label, seat_number, is_active) VALUES (hall_id, row_label, s, true);
        END LOOP;
    END LOOP;
END $$;

-- 6. Movies
INSERT INTO movies (title, description, duration_minutes, age_rating, language, subtitles, genre, poster_url, is_active) VALUES
('Diuna: Część Druga', 'Epicka kontynuacja adaptacji słynnej powieści.', 166, 'PG-13', 'Angielski', 'Polski', 'Sci-Fi', 'https://example.com/diuna2.jpg', true),
('Deadpool & Wolverine', 'Superbohaterowie w akcji.', 120, 'R', 'Angielski', 'Polski', 'Akcja / Komedia', 'https://example.com/deadpool.jpg', true),
('Kung Fu Panda 4', 'Kolejna część przygód Po.', 94, 'G', 'Polski Dubbing', NULL, 'Animacja / Familijny', 'https://example.com/panda.jpg', true),
('The Batman', 'Mroczny rycerz wraca do Gotham.', 175, 'PG-13', 'Angielski', 'Polski', 'Thriller / Akcja', 'https://example.com/batman.jpg', true),
('Oppenheimer', 'Historia twórcy bomby atomowej.', 180, 'R', 'Angielski', 'Polski', 'Dramat / Historyczny', 'https://example.com/oppenheimer.jpg', true);

-- 7. Screenings
DO $$
DECLARE
    m_id BIGINT;
    h_small BIGINT;
    h_big BIGINT;
    d DATE;
    n INT;
BEGIN
    SELECT id INTO h_big FROM cinema_halls WHERE name = 'Sala Główna';
    SELECT id INTO h_small FROM cinema_halls WHERE name = 'Sala Mała';

    -- Dla każdego filmu zróbmy 2 seanse przez następne 7 dni
    FOR n IN 1..7 LOOP
        -- Diuna
        SELECT id INTO m_id FROM movies WHERE title = 'Diuna: Część Druga';
        INSERT INTO screenings (movie_id, hall_id, start_time, end_time, base_price, status) VALUES
        (m_id, h_big, CURRENT_DATE + n + TIME '15:00:00', CURRENT_DATE + n + TIME '17:46:00', 30.00, 'ZAPLANOWANY'),
        (m_id, h_big, CURRENT_DATE + n + TIME '19:00:00', CURRENT_DATE + n + TIME '21:46:00', 35.00, 'ZAPLANOWANY');

        -- Deadpool
        SELECT id INTO m_id FROM movies WHERE title = 'Deadpool & Wolverine';
        INSERT INTO screenings (movie_id, hall_id, start_time, end_time, base_price, status) VALUES
        (m_id, h_small, CURRENT_DATE + n + TIME '15:00:00', CURRENT_DATE + n + TIME '17:00:00', 25.00, 'ZAPLANOWANY'),
        (m_id, h_small, CURRENT_DATE + n + TIME '19:00:00', CURRENT_DATE + n + TIME '21:00:00', 30.00, 'ZAPLANOWANY');

        -- Kung Fu Panda
        SELECT id INTO m_id FROM movies WHERE title = 'Kung Fu Panda 4';
        INSERT INTO screenings (movie_id, hall_id, start_time, end_time, base_price, status) VALUES
        (m_id, h_big, CURRENT_DATE + n + TIME '13:00:00', CURRENT_DATE + n + TIME '14:34:00', 20.00, 'ZAPLANOWANY');

        -- Batman
        SELECT id INTO m_id FROM movies WHERE title = 'The Batman';
        INSERT INTO screenings (movie_id, hall_id, start_time, end_time, base_price, status) VALUES
        (m_id, h_small, CURRENT_DATE + n + TIME '21:30:00', CURRENT_DATE + n + 1 + TIME '00:25:00', 35.00, 'ZAPLANOWANY');

        -- Oppenheimer
        SELECT id INTO m_id FROM movies WHERE title = 'Oppenheimer';
        INSERT INTO screenings (movie_id, hall_id, start_time, end_time, base_price, status) VALUES
        (m_id, h_big, CURRENT_DATE + n + TIME '10:00:00', CURRENT_DATE + n + TIME '13:00:00', 25.00, 'ZAPLANOWANY');
    END LOOP;
END $$;
