package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exceptions.*;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;


    @Transactional
    public BookingResponseDto createBooking(BookingRequestDto dto, Long userId) {
        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new EntityNotFoundException(String.format("Cannot make booking. " +
                        "Item with id %d not found", dto.getItemId())));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Cannot make booking. " +
                        "User with id %d not found", userId)));
        if (item.getUser() == user) {
            throw new EntityNotFoundException(String.format("item with id %d already belongs " +
                    "to user with id %d", item.getId(), userId));
        }
        if (!item.getAvailable()) {
            throw new BookingConsistencyException(String.format("item with id %d is not available for booking",
                    item.getId()));
        }
        if (dto.getStart().isAfter(dto.getEnd())) {
            throw new StartAfterEndException("end of the booking has to be after its start");
        }
        Booking booking = BookingMapper.BOOKING_MAPPER.toBooking(dto);
        booking.setItem(item);
        booking.setUser(user);
        booking.setStatus(Status.WAITING);
        log.info("booking for item with id {} created by user with id {}", item.getId(), userId);
        return BookingMapper.BOOKING_MAPPER.toDto(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponseDto approveBooking(Long bookingId, Long userId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("no booking with id %d", bookingId)));
        Item item = booking.getItem();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("no user with id %d", bookingId)));
        if (item.getUser() != user) {
            throw new WrongOwnerException(String.format("user with id %d is not an owner " +
                    "for item with id %d", userId, item.getId()));
        }
        if (booking.getStatus() == Status.APPROVED) {
            throw new BookingConsistencyException(String.format("booking with id %d is already approved", bookingId));
        }
        if (approved) {
            booking.setStatus(Status.APPROVED);
            log.info("booking with id {} approved", bookingId);
        }
        if (!approved) {
            booking.setStatus(Status.REJECTED);
            log.info("booking with id {} rejected", bookingId);
        }
        bookingRepository.updateBookingStatus(booking.getStatus(), bookingId);
        return BookingMapper.BOOKING_MAPPER.toDto(booking);
    }

    public BookingResponseDto getBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findByIdAndItem_User_IdOrUser_Id(bookingId, userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("there is no such booking with id %d " +
                        "and owner with id %d", userId, bookingId)));
        return BookingMapper.BOOKING_MAPPER.toDto(booking);
    }

    public List<BookingResponseDto> getBookingsByBooker(Long bookerId, String state, BookingPaginationParams params) {
        if (userRepository.findById(bookerId).isEmpty()) {
            throw new EntityNotFoundException(String.format("no bookings for user " +
                    "with id %d", bookerId));
        }
        switch (state) {
            case "ALL":
                List<Booking> bookings = bookingRepository.findAllByUser_Id(bookerId);
                return bookings
                        .subList(params.getFrom(), Math.min(params.getFrom() + params.getSize(), bookings.size()))
                        .stream()
                    .map(BookingMapper.BOOKING_MAPPER::toDto)
                    .collect(Collectors.toList());
            case "FUTURE":
                return bookingRepository.findFutureBookingsByBooker(
                                bookerId, PageRequest.of(params.getFrom(), params.getSize()))
                        .getContent()
                        .stream()
                        .map(BookingMapper.BOOKING_MAPPER::toDto)
                        .collect(Collectors.toList());
            case "PAST":
                return bookingRepository.findPastBookingsByBooker(
                                bookerId, PageRequest.of(params.getFrom(), params.getSize()))
                        .getContent()
                        .stream()
                        .map(BookingMapper.BOOKING_MAPPER::toDto)
                        .collect(Collectors.toList());
            case "CURRENT":
                return bookingRepository.findCurrentBookingsByBooker(
                                bookerId, PageRequest.of(params.getFrom(), params.getSize()))
                        .getContent()
                        .stream()
                        .map(BookingMapper.BOOKING_MAPPER::toDto)
                        .collect(Collectors.toList());
            case "WAITING":
                return bookingRepository.findByStatusByBooker(
                                Status.WAITING, bookerId, PageRequest.of(params.getFrom(), params.getSize()))
                        .getContent()
                        .stream()
                        .map(BookingMapper.BOOKING_MAPPER::toDto)
                        .collect(Collectors.toList());
            case "REJECTED":
                return bookingRepository.findByStatusByBooker(
                                Status.REJECTED, bookerId, PageRequest.of(params.getFrom(), params.getSize()))
                        .getContent()
                        .stream()
                        .map(BookingMapper.BOOKING_MAPPER::toDto)
                        .collect(Collectors.toList());
            default:
                throw new UnsupportedStateException(state);
        }
    }

    public List<BookingResponseDto> getBookingsByOwner(Long ownerId, String state, BookingPaginationParams params) {
        if (userRepository.findById(ownerId).isEmpty()) {
            throw new EntityNotFoundException(String.format("no bookings for user " +
                    "with id %d", ownerId));
        }
        switch (state) {
            case "ALL":
                return bookingRepository.findAllByOwner(ownerId, PageRequest.of(params.getFrom(), params.getSize()))
                        .getContent()
                        .stream()
                        .map(BookingMapper.BOOKING_MAPPER::toDto)
                        .collect(Collectors.toList());
            case "FUTURE":
                return bookingRepository.findFutureBookingsByOwner(ownerId, PageRequest.of(params.getFrom(), params.getSize()))
                        .getContent()
                        .stream()
                        .map(BookingMapper.BOOKING_MAPPER::toDto)
                        .collect(Collectors.toList());
            case "PAST":
                return bookingRepository.findPastBookingsByOwner(ownerId, PageRequest.of(params.getFrom(), params.getSize()))
                        .getContent()
                        .stream()
                        .map(BookingMapper.BOOKING_MAPPER::toDto)
                        .collect(Collectors.toList());
            case "CURRENT":
                return bookingRepository.findCurrentBookingsByOwner(ownerId, PageRequest.of(params.getFrom(), params.getSize()))
                        .getContent()
                        .stream()
                        .map(BookingMapper.BOOKING_MAPPER::toDto)
                        .collect(Collectors.toList());
            case "WAITING":
                return bookingRepository.findByStatusByOwner(
                                Status.WAITING, ownerId, PageRequest.of(params.getFrom(), params.getSize()))
                        .getContent()
                        .stream()
                        .map(BookingMapper.BOOKING_MAPPER::toDto)
                        .collect(Collectors.toList());
            case "REJECTED":
                return bookingRepository.findByStatusByOwner(
                                Status.REJECTED, ownerId, PageRequest.of(params.getFrom(), params.getSize()))
                        .getContent()
                        .stream()
                        .map(BookingMapper.BOOKING_MAPPER::toDto)
                        .collect(Collectors.toList());
            default:
                throw new UnsupportedStateException(state);
        }
    }
}
