package ru.practicum.shareit.item.dto;

import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingInItemResponseDto;
import ru.practicum.shareit.comments.dto.CommentResponseDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Data
public class ItemResponseDto {

    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private BookingInItemResponseDto lastBooking;
    private BookingInItemResponseDto nextBooking;
    private User user;
    private List<CommentResponseDto> comments;
}
