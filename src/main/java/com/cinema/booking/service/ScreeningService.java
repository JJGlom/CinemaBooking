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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScreeningService {

    private final ScreeningRepository screeningRepository;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;

    @Transactional
    public ScreeningDto createScreening(CreateScreeningDto dto) {
        Movie movie = movieRepository.findById(dto.movieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found"));

        Room room = roomRepository.findById(dto.roomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        LocalDateTime newStartTime = dto.startTime();
        LocalDateTime newEndTime = newStartTime.plusMinutes(movie.getDurationMinutes() + 20);

        validateRoomAvailability(room.getId(), newStartTime, newEndTime);

        Screening screening = Screening.builder()
                .movie(movie)
                .room(room)
                .startTime(newStartTime)
                .build();

        Screening savedScreening = screeningRepository.save(screening);

        return mapToDto(savedScreening, newEndTime);
    }

    public List<ScreeningDto> getScreeningsByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        return screeningRepository.findByStartTimeBetween(startOfDay, endOfDay).stream()
                .map(screening -> {
                    LocalDateTime endTime = screening.getStartTime()
                            .plusMinutes(screening.getMovie().getDurationMinutes() + 20);
                    return mapToDto(screening, endTime);
                })
                .collect(Collectors.toList());
    }

    private void validateRoomAvailability(Long roomId, LocalDateTime newStart, LocalDateTime newEnd) {
        List<Screening> existingScreenings = screeningRepository.findByRoomIdAndStartTimeBetween(
                roomId, newStart.minusHours(4), newEnd.plusHours(4));

        boolean overlap = existingScreenings.stream().anyMatch(s -> {
            LocalDateTime existingStart = s.getStartTime();
            LocalDateTime existingEnd = existingStart.plusMinutes(s.getMovie().getDurationMinutes() + 20);

            return newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart);
        });

        if (overlap) {
            throw new IllegalArgumentException("Room is already booked for this time interval");
        }
    }

    private ScreeningDto mapToDto(Screening screening, LocalDateTime endTime) {
        return ScreeningDto.builder()
                .id(screening.getId())
                .movieId(screening.getMovie().getId())
                .movieTitle(screening.getMovie().getTitle())
                .roomId(screening.getRoom().getId())
                .roomName(screening.getRoom().getName())
                .startTime(screening.getStartTime())
                .endTime(endTime)
                .build();
    }
}