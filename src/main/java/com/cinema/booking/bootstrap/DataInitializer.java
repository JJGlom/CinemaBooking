package com.cinema.booking.bootstrap;

import com.cinema.booking.model.*;
import com.cinema.booking.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoomRepository roomRepository;
    private final MovieRepository movieRepository;
    private final ScreeningRepository screeningRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        ticketRepository.deleteAll();
        screeningRepository.deleteAll();

        initData();
    }

    private void initData() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin"))
                    .role("ROLE_ADMIN")
                    .build();
            userRepository.save(admin);
        }

        if (userRepository.findByUsername("user").isEmpty()) {
            User user = User.builder()
                    .username("user")
                    .password(passwordEncoder.encode("user"))
                    .role("ROLE_USER")
                    .build();
            userRepository.save(user);
        }

        if (roomRepository.count() == 0) {
            Room roomA = Room.builder()
                    .name("Sala A (IMAX)")
                    .capacity(100)
                    .build();

            List<Seat> seats = new ArrayList<>();
            for (int row = 1; row <= 10; row++) {
                for (int number = 1; number <= 10; number++) {
                    seats.add(Seat.builder()
                            .room(roomA)
                            .rowNumber(row)
                            .seatNumber(number)
                            .build());
                }
            }
            roomA.setSeats(seats);
            roomRepository.save(roomA);

            if (movieRepository.count() == 0) {
                Movie movie = Movie.builder()
                        .title("Diuna: Część druga")
                        .description("Opis filmu...")
                        .genre("Sci-Fi")
                        .director("Denis Villeneuve")
                        .durationMinutes(166)
                        .ageRestriction(13)
                        .build();

                movieRepository.save(movie);

                Screening screeningToday = Screening.builder()
                        .movie(movie)
                        .room(roomA)
                        .startTime(LocalDateTime.now().plusHours(2))
                        .build();
                screeningRepository.save(screeningToday);
            }
        }
    }
}