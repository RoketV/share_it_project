package ru.practicum.shareit.booking.dto;

import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.booking.model.Booking;

 public interface BookingMapper {

    BookingMapper BOOKING_MAPPER = Mappers.getMapper(BookingMapper.class);

    Booking toBooking(BookingRequestDto dto);

    BookingResponseDto toDto(Booking booking);
}
