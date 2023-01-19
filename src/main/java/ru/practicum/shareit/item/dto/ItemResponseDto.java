package ru.practicum.shareit.item.dto;

import lombok.Data;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.user.model.User;

@Data
public class ItemResponseDto {

    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private Booking lastBooking;
    private Booking nextBooking;
    private User user;
}
