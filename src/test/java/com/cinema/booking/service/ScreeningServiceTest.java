package com.cinema.booking.service;

import com.cinema.booking.dto.CreateScreeningDto;
import com.cinema.booking.dto.ScreeningDto;
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
    void shouldThrowExceptionWhenScreeningOverlaps() {
        LocalDateTime newStart = LocalDateTime.now().plusHours(2);
        CreateScreeningDto dto = new CreateScreeningDto(1L, 1L, newStart);

        Movie movie = Movie.builder().id(1L).durationMinutes(120).build();
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

        verify(screeningRepository, never()).save(any(Screening.class));
    }
}