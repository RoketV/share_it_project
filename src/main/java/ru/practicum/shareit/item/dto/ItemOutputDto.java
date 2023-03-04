package ru.practicum.shareit.item.dto;

import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingInItemResponseDto;
import ru.practicum.shareit.comments.dto.CommentOutputDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Data
public class ItemOutputDto {

    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private BookingInItemResponseDto lastBooking;
    private BookingInItemResponseDto nextBooking;
    private User user;
    private Long requestId;
    private List<CommentOutputDto> comments;
}
