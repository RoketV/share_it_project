package ru.practicum.shareit.booking.dto;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.model.Booking;

@Component
public class BookingMapperImpl implements BookingMapper {

    @Override
    public Booking toBooking(BookingRequestDto dto) {
        if (dto == null) {
            return null;
        }
        Booking booking = new Booking();
        booking.setStart(dto.getStart());
        booking.setEnd(dto.getEnd());
        return booking;
    }

    @Override
    public BookingResponseDto toDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        BookingResponseDto dto = new BookingResponseDto();
        dto.setId(booking.getId());
        dto.setItem(booking.getItem());
        dto.setStatus(booking.getStatus());
        dto.setUser(booking.getUser());
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());
        return dto;
    }
}
