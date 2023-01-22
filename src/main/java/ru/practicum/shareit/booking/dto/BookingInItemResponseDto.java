package ru.practicum.shareit.booking.dto;

import lombok.Data;
import ru.practicum.shareit.booking.enums.Status;

import java.time.LocalDateTime;

@Data
public class BookingInItemResponseDto {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private Long itemId;
    private Long bookerId;
    private Status status;
}
