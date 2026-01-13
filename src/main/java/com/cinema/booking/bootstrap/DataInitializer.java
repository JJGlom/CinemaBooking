package com.cinema.booking.bootstrap;

import com.cinema.booking.model.Movie;
import com.cinema.booking.model.Room;
import com.cinema.booking.model.Screening;
import com.cinema.booking.model.Seat;
import com.cinema.booking.repository.MovieRepository;
import com.cinema.booking.repository.RoomRepository;
import com.cinema.booking.repository.ScreeningRepository;
import com.cinema.booking.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
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

    @Override
    @Transactional
    public void run(String... args) {
        ticketRepository.deleteAll();
        screeningRepository.deleteAll();
        movieRepository.deleteAll();
        roomRepository.deleteAll();

        initData();
    }

    private void initData() {
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

        Screening screeningTomorrow = Screening.builder()
                .movie(movie)
                .room(roomA)
                .startTime(LocalDateTime.now().plusDays(1).withHour(18).withMinute(0))
                .build();
        screeningRepository.save(screeningTomorrow);
    }
}