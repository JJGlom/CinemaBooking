package com.cinema.booking.service;

import com.cinema.booking.dto.CreateScreeningDto;
import com.cinema.booking.dto.ScreeningDto;
import com.cinema.booking.exception.ResourceNotFoundException;
import com.cinema.booking.model.Movie;
import com.cinema.booking.model.Room;
import com.cinema.booking.model.Screening;
import com.cinema.booking.repository.MovieRepository;
import com.cinema.booking.repository.RoomRepository;
import com.cinema.booking.repository.ScreeningRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScreeningServiceTest {

    @Mock
    private ScreeningRepository screeningRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private ScreeningService screeningService;

    @Test
    void shouldCreateScreeningWhenRoomIsAvailable() {
        CreateScreeningDto dto = new CreateScreeningDto(1L, 1L, LocalDateTime.now().plusHours(2));
        Movie movie = Movie.builder().id(1L).title("Test Movie").durationMinutes(120).build();
        Room room = Room.builder().id(1L).name("Test Room").build();

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(screeningRepository.findByRoomIdAndStartTimeBetween(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        Screening savedScreening = Screening.builder()
                .id(100L)
                .movie(movie)
                .room(room)
                .startTime(dto.startTime())
                .build();

        when(screeningRepository.save(any(Screening.class))).thenReturn(savedScreening);

        ScreeningDto result = screeningService.createScreening(dto);

        assertThat(result.id()).isEqualTo(100L);
        assertThat(result.movieTitle()).isEqualTo("Test Movie");
        verify(screeningRepository).save(any(Screening.class));
    }

    @Test
    void shouldThrowExceptionWhenMovieNotFound() {
        CreateScreeningDto dto = new CreateScreeningDto(999L, 1L, LocalDateTime.now());
        when(movieRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> screeningService.createScreening(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Movie not found");
    }

    @Test
    void shouldThrowExceptionWhenRoomNotFound() {
        CreateScreeningDto dto = new CreateScreeningDto(1L, 999L, LocalDateTime.now());
        when(movieRepository.findById(1L)).thenReturn(Optional.of(new Movie()));
        when(roomRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> screeningService.createScreening(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Room not found");
    }

    @Test
    void shouldThrowExceptionWhenScreeningOverlaps() {
        LocalDateTime newStart = LocalDateTime.now().plusHours(2);
        CreateScreeningDto dto = new CreateScreeningDto(1L, 1L, newStart);

        Movie movie = Movie.builder().id(1L).durationMinutes(60).build();
        Room room = Room.builder().id(1L).build();

        Screening existingScreening = Screening.builder()
                .id(50L)
                .movie(movie)
                .room(room)
                .startTime(newStart.plusMinutes(30))
                .build();

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(screeningRepository.findByRoomIdAndStartTimeBetween(any(), any(), any()))
                .thenReturn(List.of(existingScreening));

        assertThatThrownBy(() -> screeningService.createScreening(dto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowExceptionWhenScreeningIsInsideExisting() {
        LocalDateTime existingStart = LocalDateTime.of(2030, 1, 1, 10, 0);
        LocalDateTime newStart = LocalDateTime.of(2030, 1, 1, 10, 30);

        CreateScreeningDto dto = new CreateScreeningDto(1L, 1L, newStart);

        Movie newMovie = Movie.builder().id(1L).durationMinutes(40).build();
        Movie existingMovie = Movie.builder().id(1L).durationMinutes(100).build();
        Room room = Room.builder().id(1L).build();

        Screening existingScreening = Screening.builder()
                .id(50L)
                .movie(existingMovie)
                .room(room)
                .startTime(existingStart)
                .build();

        when(movieRepository.findById(1L)).thenReturn(Optional.of(newMovie));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(screeningRepository.findByRoomIdAndStartTimeBetween(any(), any(), any()))
                .thenReturn(List.of(existingScreening));

        assertThatThrownBy(() -> screeningService.createScreening(dto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowExceptionWhenScreeningEnvelopsExisting() {
        LocalDateTime existingStart = LocalDateTime.of(2030, 1, 1, 11, 0);
        LocalDateTime newStart = LocalDateTime.of(2030, 1, 1, 10, 0);

        CreateScreeningDto dto = new CreateScreeningDto(1L, 1L, newStart);

        Movie newMovie = Movie.builder().id(1L).durationMinutes(160).build();
        Movie existingMovie = Movie.builder().id(1L).durationMinutes(40).build();
        Room room = Room.builder().id(1L).build();

        Screening existingScreening = Screening.builder()
                .id(50L)
                .movie(existingMovie)
                .room(room)
                .startTime(existingStart)
                .build();

        when(movieRepository.findById(1L)).thenReturn(Optional.of(newMovie));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(screeningRepository.findByRoomIdAndStartTimeBetween(any(), any(), any()))
                .thenReturn(List.of(existingScreening));

        assertThatThrownBy(() -> screeningService.createScreening(dto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldAllowScreeningAdjacentToExisting() {
        LocalDateTime newStart = LocalDateTime.now().plusHours(5);
        CreateScreeningDto dto = new CreateScreeningDto(1L, 1L, newStart);

        Movie movie = Movie.builder().id(1L).durationMinutes(100).build();
        Room room = Room.builder().id(1L).build();

        LocalDateTime existingStart = newStart.minusMinutes(120);
        Screening existingScreening = Screening.builder()
                .id(50L)
                .movie(movie)
                .room(room)
                .startTime(existingStart)
                .build();

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(screeningRepository.findByRoomIdAndStartTimeBetween(any(), any(), any()))
                .thenReturn(List.of(existingScreening));

        Screening savedScreening = Screening.builder().id(100L).movie(movie).room(room).startTime(newStart).build();
        when(screeningRepository.save(any(Screening.class))).thenReturn(savedScreening);

        ScreeningDto result = screeningService.createScreening(dto);
        assertThat(result).isNotNull();
    }

    @Test
    void shouldAllowScreeningBeforeExistingWithoutOverlap() {
        LocalDateTime newStart = LocalDateTime.now().plusHours(1);
        CreateScreeningDto dto = new CreateScreeningDto(1L, 1L, newStart);

        Movie newMovie = Movie.builder().id(1L).durationMinutes(60).build();
        Movie existingMovie = Movie.builder().id(1L).durationMinutes(100).build();
        Room room = Room.builder().id(1L).build();

        LocalDateTime existingStart = newStart.plusMinutes(90);
        Screening existingScreening = Screening.builder()
                .id(50L)
                .movie(existingMovie)
                .room(room)
                .startTime(existingStart)
                .build();

        when(movieRepository.findById(1L)).thenReturn(Optional.of(newMovie));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        when(screeningRepository.findByRoomIdAndStartTimeBetween(any(), any(), any()))
                .thenReturn(List.of(existingScreening));

        Screening savedScreening = Screening.builder().id(100L).movie(newMovie).room(room).startTime(newStart).build();
        when(screeningRepository.save(any(Screening.class))).thenReturn(savedScreening);

        ScreeningDto result = screeningService.createScreening(dto);
        assertThat(result).isNotNull();
    }

    @Test
    void shouldGetScreeningsByDate() {
        LocalDate date = LocalDate.now();
        Movie movie = Movie.builder().id(1L).title("Movie").durationMinutes(100).build();
        Room room = Room.builder().id(1L).name("Room").build();
        Screening screening = Screening.builder()
                .id(1L)
                .movie(movie)
                .room(room)
                .startTime(date.atTime(12, 0))
                .build();

        when(screeningRepository.findByStartTimeBetween(any(), any())).thenReturn(List.of(screening));

        List<ScreeningDto> result = screeningService.getScreeningsByDate(date);

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldDeleteScreening() {
        Long id = 1L;
        when(screeningRepository.existsById(id)).thenReturn(true);
        screeningService.deleteScreening(id);
        verify(screeningRepository).deleteById(id);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentScreening() {
        Long id = 1L;
        when(screeningRepository.existsById(id)).thenReturn(false);
        assertThatThrownBy(() -> screeningService.deleteScreening(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}