package com.cinema.booking.service;

import com.cinema.booking.model.Room;
import com.cinema.booking.repository.RoomRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private RoomService roomService;

    @Test
    void shouldGetAllRooms() {
        Room room = new Room();
        room.setName("Room A");

        when(roomRepository.findAll()).thenReturn(List.of(room));

        List<Room> result = roomService.getAllRooms();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Room A");
    }
}