package com.cinema.booking.bootstrap;

import com.cinema.booking.model.Movie;
import com.cinema.booking.model.Room;
import com.cinema.booking.model.Seat;
import com.cinema.booking.repository.MovieRepository;
import com.cinema.booking.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoomRepository roomRepository;
    private final MovieRepository movieRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (roomRepository.count() == 0) {
            initData();
        }
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
                .description("Książę Paul Atryda przyjmuje przydomek Muad'Dib...")
                .genre("Sci-Fi")
                .director("Denis Villeneuve")
                .durationMinutes(166)
                .ageRestriction(13)
                .build();

        movieRepository.save(movie);
    }
}