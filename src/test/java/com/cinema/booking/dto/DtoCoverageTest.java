package com.cinema.booking.dto;

import com.cinema.booking.model.TicketType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DtoCoverageTest {

    @Test
    void shouldCoverBookTicketDto() {
        TicketSelection ts = new TicketSelection(1L, TicketType.NORMAL);
        BookTicketDto dto = new BookTicketDto(1L, List.of(ts));

        assertThat(dto.screeningId()).isEqualTo(1L);
        assertThat(dto.tickets()).hasSize(1);
        assertThat(ts.seatId()).isEqualTo(1L);
        assertThat(ts.ticketType()).isEqualTo(TicketType.NORMAL);
    }

    @Test
    void shouldCoverCreateScreeningDto() {
        LocalDateTime time = LocalDateTime.now();
        CreateScreeningDto dto = new CreateScreeningDto(1L, 2L, time);

        assertThat(dto.movieId()).isEqualTo(1L);
        assertThat(dto.roomId()).isEqualTo(2L);
        assertThat(dto.startTime()).isEqualTo(time);
    }

    @Test
    void shouldCoverDailyStatsDto() {
        LocalDate date = LocalDate.now();
        DailyStatsDto dto = new DailyStatsDto(date, 100L, BigDecimal.TEN);

        assertThat(dto.date()).isEqualTo(date);
        assertThat(dto.ticketsSold()).isEqualTo(100L);
        assertThat(dto.totalRevenue()).isEqualTo(BigDecimal.TEN);
    }

    @Test
    void shouldCoverMovieDto() {
        MovieDto dto = MovieDto.builder()
                .id(1L)
                .title("Title")
                .description("Desc")
                .genre("Genre")
                .director("Dir")
                .durationMinutes(120)
                .ageRestriction(12)
                .posterUrl("url")
                .trailerUrl("trailer")
                .castMembers("Cast")
                .build();

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.title()).isEqualTo("Title");
        assertThat(dto.description()).isEqualTo("Desc");
        assertThat(dto.genre()).isEqualTo("Genre");
        assertThat(dto.director()).isEqualTo("Dir");
        assertThat(dto.durationMinutes()).isEqualTo(120);
        assertThat(dto.ageRestriction()).isEqualTo(12);
        assertThat(dto.posterUrl()).isEqualTo("url");
        assertThat(dto.trailerUrl()).isEqualTo("trailer");
        assertThat(dto.castMembers()).isEqualTo("Cast");
    }

    @Test
    void shouldCoverMovieStatsDto() {
        MovieStatsDto dto = new MovieStatsDto("Title", 50L, BigDecimal.ONE);

        assertThat(dto.movieTitle()).isEqualTo("Title");
        assertThat(dto.ticketsSold()).isEqualTo(50L);
        assertThat(dto.totalRevenue()).isEqualTo(BigDecimal.ONE);
    }

    @Test
    void shouldCoverSeatDto() {
        SeatDto dto = SeatDto.builder()
                .id(1L)
                .rowNumber(1)
                .seatNumber(2)
                .available(true)
                .build();

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.rowNumber()).isEqualTo(1);
        assertThat(dto.seatNumber()).isEqualTo(2);
        assertThat(dto.available()).isTrue();
    }

    @Test
    void shouldCoverScreeningDto() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusHours(2);
        ScreeningDto dto = ScreeningDto.builder()
                .id(1L)
                .movieId(10L)
                .movieTitle("Title")
                .posterUrl("url")
                .roomId(20L)
                .roomName("Room")
                .startTime(start)
                .endTime(end)
                .build();

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.movieId()).isEqualTo(10L);
        assertThat(dto.movieTitle()).isEqualTo("Title");
        assertThat(dto.posterUrl()).isEqualTo("url");
        assertThat(dto.roomId()).isEqualTo(20L);
        assertThat(dto.roomName()).isEqualTo("Room");
        assertThat(dto.startTime()).isEqualTo(start);
        assertThat(dto.endTime()).isEqualTo(end);
    }
}