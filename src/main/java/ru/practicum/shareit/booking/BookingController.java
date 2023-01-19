package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.State;
import ru.practicum.shareit.exceptions.UnsupportedStateException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * TODO Sprint add-bookings.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponseDto> createBooking(@Valid @RequestBody BookingRequestDto dto,
                                                            @RequestHeader("X-Sharer-User-Id") @NotNull Long userId) {
        return ResponseEntity.ok(bookingService.createBooking(dto, userId));
    }

    @PatchMapping("{bookingId}")
    public ResponseEntity<BookingResponseDto> patchBooking(@PathVariable Long bookingId,
                                                           @RequestHeader("X-Sharer-User-Id") @NotNull Long userId,
                                                           @RequestParam Boolean approved) {
        return ResponseEntity.ok(bookingService.approveBooking(bookingId, userId, approved));
    }

    @GetMapping("{bookingId}")
    public ResponseEntity<BookingResponseDto> getBooking(@PathVariable Long bookingId,
                                                         @RequestHeader("X-Sharer-User-Id") @NotNull Long userId) {
        return ResponseEntity.ok(bookingService.getBooking(bookingId, userId));
    }

    @GetMapping
    public ResponseEntity<List<BookingResponseDto>> getBookingsByBooker(@RequestHeader("X-Sharer-User-Id") @NotNull Long bookerId,
                                                                        @RequestParam(required = false,
                                                                                defaultValue = "ALL") String state) {
        try {
            return ResponseEntity.ok(bookingService.getBookingsByBooker(bookerId, state));
        } catch (IllegalArgumentException e) {
            throw new UnsupportedStateException(state.toString());
        }
    }
    @GetMapping("/owner")
    public ResponseEntity<List<BookingResponseDto>> getBookingsByOwner(@RequestHeader("X-Sharer-User-Id") @NotNull Long ownerId,
                                                                       @RequestParam(required = false,
                                                                               defaultValue = "ALL") String state) {
            return ResponseEntity.ok(bookingService.getBookingsByOwner(ownerId, state));
    }
}
