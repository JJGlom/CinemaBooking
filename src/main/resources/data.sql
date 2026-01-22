DELETE FROM users;

INSERT INTO users (username, password, role) VALUES ('admin', '{noop}pass', 'ROLE_ADMIN');
INSERT INTO users (username, password, role) VALUES ('user', '{noop}pass', 'ROLE_USER');

INSERT INTO rooms (id, name, capacity)
SELECT 1, 'Sala A (IMAX)', 100
WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE id = 1);

INSERT INTO rooms (id, name, capacity)
SELECT 2, 'Sala B (Kameralna)', 50
WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE id = 2);

SELECT setval('rooms_id_seq', (SELECT MAX(id) FROM rooms));

INSERT INTO seats (room_id, row_number, seat_number)
SELECT 1, r, s
FROM generate_series(1, 10) as r
CROSS JOIN generate_series(1, 10) as s
WHERE NOT EXISTS (SELECT 1 FROM seats WHERE room_id = 1);

INSERT INTO seats (room_id, row_number, seat_number)
SELECT 2, r, s
FROM generate_series(1, 5) as r
CROSS JOIN generate_series(1, 10) as s
WHERE NOT EXISTS (SELECT 1 FROM seats WHERE room_id = 2);

-- Filmy
INSERT INTO movies (title, description, genre, director, duration_minutes, age_restriction, poster_url, trailer_url)
SELECT 'Diuna: Część druga', 'Książę Paul Atryda przyjmuje przydomek Muad''Dib i rozpoczyna duchowo-fizyczną podróż.', 'Sci-Fi', 'Denis Villeneuve', 166, 13, 'https://image.tmdb.org/t/p/original/8b8R8l88Qje9dn9OE8PY05Nxl1X.jpg', 'https://www.youtube.com/embed/Way9Dexny3w'
WHERE NOT EXISTS (SELECT 1 FROM movies WHERE title = 'Diuna: Część druga');

INSERT INTO movies (title, description, genre, director, duration_minutes, age_restriction, poster_url, trailer_url)
SELECT 'Oppenheimer', 'Historia amerykańskiego naukowca J. Roberta Oppenheimera i jego roli w stworzeniu bomby atomowej.', 'Biograficzny', 'Christopher Nolan', 180, 16, 'https://image.tmdb.org/t/p/original/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg', 'https://www.youtube.com/embed/uYPbbksJxIg'
WHERE NOT EXISTS (SELECT 1 FROM movies WHERE title = 'Oppenheimer');

INSERT INTO actors (name) VALUES ('Timothée Chalamet'), ('Zendaya'), ('Cillian Murphy'), ('Emily Blunt');

INSERT INTO movie_actors (movie_id, actor_id)
SELECT m.id, a.id FROM movies m, actors a WHERE m.title = 'Diuna: Część druga' AND a.name IN ('Timothée Chalamet', 'Zendaya');

INSERT INTO movie_actors (movie_id, actor_id)
SELECT m.id, a.id FROM movies m, actors a WHERE m.title = 'Oppenheimer' AND a.name IN ('Cillian Murphy', 'Emily Blunt');


INSERT INTO screenings (movie_id, room_id, start_time)
SELECT
    (SELECT id FROM movies WHERE title = 'Diuna: Część druga' LIMIT 1),
    1,
    CURRENT_TIMESTAMP + INTERVAL '2' HOUR
WHERE NOT EXISTS (SELECT 1 FROM screenings WHERE room_id = 1 AND start_time > CURRENT_TIMESTAMP);

INSERT INTO screenings (movie_id, room_id, start_time)
SELECT
    (SELECT id FROM movies WHERE title = 'Oppenheimer' LIMIT 1),
    2,
    CURRENT_TIMESTAMP + INTERVAL '4' HOUR
WHERE NOT EXISTS (SELECT 1 FROM screenings WHERE room_id = 2 AND start_time > CURRENT_TIMESTAMP);