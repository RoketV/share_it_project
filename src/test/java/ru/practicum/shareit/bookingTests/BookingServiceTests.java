package ru.practicum.shareit.bookingTests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.booking.BookingPaginationParams;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingService;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ActiveProfiles("test")
@Transactional
public class BookingServiceTests {

    @MockBean
    private BookingRepository bookingRepository;

    @MockBean
    private ItemRepository itemRepository;

    @MockBean
    private UserRepository userRepository;


    private BookingService bookingService;

    @BeforeEach
    public void setUp() {
        bookingService = new BookingService(bookingRepository, itemRepository, userRepository);
    }

    @Test
    public void createBooking_withValidData_returnsBookingResponseDto() {
        Long userId1 = 1L;
        Long userId2 = 2L;
        Long itemId = 2L;
        User user1 = new User(userId1, "John Doe", "email@email.com");
        User user2 = new User(userId2, "John Doe", "email@email.com");
        Item item = new Item(itemId, "Item 1", "description item1", true, user1);

        BookingRequestDto bookingRequestDto = new BookingRequestDto(itemId, LocalDateTime.now(), LocalDateTime.now().plusHours(1));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userRepository.findById(userId2)).thenReturn(Optional.of(user2));
        when(bookingRepository.save(any(Booking.class))).thenReturn(new Booking());

        BookingResponseDto bookingResponseDto = bookingService.createBooking(bookingRequestDto, userId2);

        assertNotNull(bookingResponseDto);

        verify(itemRepository).findById(itemId);
        verify(userRepository).findById(userId2);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    public void createBooking_withStartAfterFinish_throwsException() {
        Long userId1 = 1L;
        Long userId2 = 2L;
        Long itemId = 2L;
        User user1 = new User(userId1, "John Doe", "email@email.com");
        User user2 = new User(userId2, "John Doe", "email@email.com");
        Item item = new Item(itemId, "Item 1", "description item1", true, user1);

        BookingRequestDto bookingRequestDto = new BookingRequestDto(itemId, LocalDateTime.now().plusHours(1), LocalDateTime.now());
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userRepository.findById(userId2)).thenReturn(Optional.of(user2));

        assertThrows(StartAfterEndException.class,
                () -> bookingService.createBooking(bookingRequestDto, userId2));

        verify(itemRepository).findById(itemId);
        verify(userRepository).findById(userId2);
    }

    @Test()
    public void createBooking_withInvalidItemId_throwsEntityNotFoundException() {
        Long userId = 1L;
        Long itemId = 2L;
        BookingRequestDto bookingRequestDto = new BookingRequestDto(itemId, LocalDateTime.now(), LocalDateTime.now().plusHours(1));
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> bookingService.createBooking(bookingRequestDto, userId));

        verify(itemRepository).findById(itemId);
    }

    @Test()
    public void createBooking_withInvalidUserId_throwsEntityNotFoundException() {
        Long userId = 1L;
        Long itemId = 2L;
        Item item = new Item(itemId, "Item 1", "description", true, new User());
        BookingRequestDto bookingRequestDto = new BookingRequestDto(itemId, LocalDateTime.now(), LocalDateTime.now().plusHours(1));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> bookingService.createBooking(bookingRequestDto, userId));

        verify(itemRepository).findById(itemId);
        verify(userRepository).findById(userId);
    }

    @Test
    public void createBooking_withSameUserAndItemId_throwsEntityNotFoundException() {
        Long userId = 1L;
        Long itemId = 2L;
        User user = new User(userId, "John Doe", "email@email.com");
        Item item = new Item(itemId, "Item 1", "description", true, user);
        BookingRequestDto bookingRequestDto = new BookingRequestDto(itemId, LocalDateTime.now(), LocalDateTime.now().plusHours(1));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(EntityNotFoundException.class,
                () -> bookingService.createBooking(bookingRequestDto, userId));

        verify(itemRepository).findById(itemId);
        verify(userRepository).findById(userId);
    }

    @Test
    public void createBooking_withUnavailableItem_throwsBookingConsistencyException() {
        Long userId = 1L;
        Long itemId = 2L;
        User user = new User(userId, "John Doe", "email@email.com");
        Item item = new Item(itemId, "Item 1", "description", false, new User());
        BookingRequestDto bookingRequestDto = new BookingRequestDto(itemId, LocalDateTime.now(), LocalDateTime.now().plusHours(1));

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(BookingConsistencyException.class,
                () -> bookingService.createBooking(bookingRequestDto, userId));

        verify(itemRepository).findById(itemId);
        verify(userRepository).findById(userId);
    }

    @Test
    public void approveBooking_withValidData_approvesBooking() {
        Long bookingId = 1L;
        Long userId = 2L;
        User user = new User(userId, "John Doe", "email@email.com");
        Booking booking = new Booking(bookingId, LocalDateTime.now(), LocalDateTime.now().plusHours(1), Status.WAITING);
        Item item = new Item(2L, "Item 1", "description", true, user);
        booking.setItem(item);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        BookingResponseDto result = bookingService.approveBooking(bookingId, userId, true);

        assertNotNull(result);
        assertEquals(Status.APPROVED, result.getStatus());

        verify(bookingRepository, times(1)).findById(bookingId);
        verify(userRepository, times(1)).findById(userId);
        verify(bookingRepository, times(1)).updateBookingStatus(Status.APPROVED, bookingId);
    }

    @Test
    public void approveBooking_withNonExistentBooking_throwsEntityNotFoundException() {
        Long bookingId = 1L;
        Long userId = 2L;

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> bookingService.approveBooking(bookingId, userId, true));
    }

    @Test
    public void approveBooking_withNonExistentUser_throwsEntityNotFoundException() {
        Long bookingId = 1L;
        Long userId = 2L;
        Booking booking = new Booking(bookingId, LocalDateTime.now(), LocalDateTime.now().plusHours(1), Status.WAITING);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> bookingService.approveBooking(bookingId, userId, true));
    }

    @Test
    public void approveBooking_withNonMatchingUser_throwsWrongOwnerException() {
        Long bookingId = 1L;
        Long userId = 2L;
        Booking booking = new Booking(bookingId, LocalDateTime.now(), LocalDateTime.now().plusHours(1), Status.WAITING);
        User user = new User(userId, "John Doe", "email@email.ua");
        Item item = new Item(2L, "Item 1", "description", true, new User());
        booking.setItem(item);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        assertThrows(WrongOwnerException.class,
                () -> bookingService.approveBooking(bookingId, userId, true));

        verify(bookingRepository, times(1)).findById(bookingId);
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    public void getBooking_withValidData_returnsBookingResponseDto() {
        Long userId = 1L;
        Long itemId = 2L;
        Long bookingId = 3L;
        User user = new User(userId, "John Doe", "email@email.com");
        Item item = new Item(itemId, "Item 1", "description", true, user);
        Booking booking = new Booking(bookingId, LocalDateTime.now(), LocalDateTime.now().plusHours(1), item, user, Status.WAITING);
        when(bookingRepository.findByIdAndItem_User_IdOrUser_Id(bookingId, userId)).thenReturn(Optional.of(booking));

        BookingResponseDto bookingResponseDto = bookingService.getBooking(bookingId, userId);

        assertNotNull(bookingResponseDto);
        verify(bookingRepository).findByIdAndItem_User_IdOrUser_Id(bookingId, userId);
    }

    @Test
    public void getBooking_withInvalidBookingIdAndUserId_throwsEntityNotFoundException() {

        Long userId = 1L;
        Long bookingId = 2L;
        when(bookingRepository.findByIdAndItem_User_IdOrUser_Id(bookingId, userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> bookingService.getBooking(bookingId, userId));
    }

    @Test
    public void getBookingsByBooker_withValidParamsAndAllState_returnsExpectedBookings() {
        Long bookerId = 1L;
        String state = "ALL";
        BookingPaginationParams params = new BookingPaginationParams(0, 10);
        User user = new User(bookerId, "John Doe", "email@email.com");
        List<Booking> bookings = Arrays.asList(
                new Booking(1L, LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                        new Item(1L, "Item 1", "description 1", true, user), user, Status.APPROVED),
                new Booking(2L, LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                        new Item(2L, "Item 2", "description 2", true, user), user, Status.REJECTED),
                new Booking(3L, LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                        new Item(3L, "Item 3", "description3", true, user), user, Status.WAITING));
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(user));
        when(bookingRepository.findAllByUser_Id(bookerId)).thenReturn(bookings);

        List<BookingResponseDto> result = bookingService.getBookingsByBooker(bookerId, state, params);

        assertEquals(bookings.size(), result.size());
        assertEquals(bookings.get(0).getId(), result.get(0).getId());
        assertEquals(bookings.get(1).getId(), result.get(1).getId());
        assertEquals(bookings.get(2).getId(), result.get(2).getId());
        verify(userRepository).findById(bookerId);
        verify(bookingRepository).findAllByUser_Id(bookerId);
    }

    @Test
    public void getBookingsByBooker_withInvalidUserId_throwsEntityNotFoundException() {
        Long bookerId = 1L;
        String state = "ALL";
        BookingPaginationParams params = new BookingPaginationParams(0, 10);
        when(userRepository.findById(bookerId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> bookingService.getBookingsByBooker(bookerId, state, params));
    }

    @Test
    public void getBookingsByBooker_withValidParamsAndFutureState_returnsExpectedBookings() {
        Long bookerId = 1L;
        String state = "FUTURE";
        BookingPaginationParams params = new BookingPaginationParams(0, 10);
        User user = new User(bookerId, "John Doe", "email@email.com");
        List<Booking> bookings = Arrays.asList(
                new Booking(1L, LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                        new Item(1L, "Item 1", "description 1", true, user), user, Status.APPROVED),
                new Booking(2L, LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                        new Item(2L, "Item 2", "description 2", true, user), user, Status.REJECTED),
                new Booking(3L, LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                        new Item(3L, "Item 3", "description3", true, user), user, Status.WAITING));
        Page<Booking> bookingPage = new PageImpl<>(bookings);
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(user));
        when(bookingRepository.findFutureBookingsByBooker(bookerId, PageRequest.of(params.getFrom(), params.getSize())))
                .thenReturn(bookingPage);

        List<BookingResponseDto> result = bookingService.getBookingsByBooker(bookerId, state, params);

        assertEquals(bookings.size(), result.size());
        assertEquals(bookings.get(0).getId(), result.get(0).getId());
        assertEquals(bookings.get(1).getId(), result.get(1).getId());
        assertEquals(bookings.get(2).getId(), result.get(2).getId());
        verify(userRepository).findById(bookerId);
        verify(bookingRepository).findFutureBookingsByBooker(bookerId, PageRequest.of(params.getFrom(), params.getSize()));
    }

    @Test
    public void getBookingsByBooker_withValidParamsAndPastState_returnsExpectedBookings() {
        Long bookerId = 1L;
        String state = "PAST";
        BookingPaginationParams params = new BookingPaginationParams(0, 10);
        User user = new User(bookerId, "John Doe", "email@email.com");
        List<Booking> bookings = Arrays.asList(
                new Booking(1L, LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                        new Item(1L, "Item 1", "description 1", true, user), user, Status.APPROVED),
                new Booking(2L, LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                        new Item(2L, "Item 2", "description 2", true, user), user, Status.REJECTED),
                new Booking(3L, LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                        new Item(3L, "Item 3", "description3", true, user), user, Status.WAITING));
        Page<Booking> bookingPage = new PageImpl<>(bookings);
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(user));
        when(bookingRepository.findPastBookingsByBooker(bookerId, PageRequest.of(params.getFrom(), params.getSize())))
                .thenReturn(bookingPage);

        List<BookingResponseDto> result = bookingService.getBookingsByBooker(bookerId, state, params);

        assertEquals(bookings.size(), result.size());
        assertEquals(bookings.get(0).getId(), result.get(0).getId());
        assertEquals(bookings.get(1).getId(), result.get(1).getId());
        assertEquals(bookings.get(2).getId(), result.get(2).getId());
        verify(userRepository).findById(bookerId);
        verify(bookingRepository).findPastBookingsByBooker(bookerId, PageRequest.of(params.getFrom(), params.getSize()));
    }

    @Test
    public void getBookingsByBooker_withValidParamsAndCurrentState_returnsExpectedBookings() {
        Long bookerId = 1L;
        String state = "CURRENT";
        BookingPaginationParams params = new BookingPaginationParams(0, 10);
        User user = new User(bookerId, "John Doe", "email@email.com");
        List<Booking> bookings = Arrays.asList(
                new Booking(1L, LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                        new Item(1L, "Item 1", "description 1", true, user), user, Status.APPROVED),
                new Booking(2L, LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                        new Item(2L, "Item 2", "description 2", true, user), user, Status.REJECTED),
                new Booking(3L, LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                        new Item(3L, "Item 3", "description3", true, user), user, Status.WAITING));
        Page<Booking> bookingPage = new PageImpl<>(bookings);
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(user));
        when(bookingRepository.findCurrentBookingsByBooker(bookerId, PageRequest.of(params.getFrom(), params.getSize())))
                .thenReturn(bookingPage);

        List<BookingResponseDto> result = bookingService.getBookingsByBooker(bookerId, state, params);

        assertEquals(bookings.size(), result.size());
        assertEquals(bookings.get(0).getId(), result.get(0).getId());
        assertEquals(bookings.get(1).getId(), result.get(1).getId());
        assertEquals(bookings.get(2).getId(), result.get(2).getId());
        verify(userRepository).findById(bookerId);
        verify(bookingRepository).findCurrentBookingsByBooker(bookerId, PageRequest.of(params.getFrom(), params.getSize()));
    }

    @Test
    public void getBookingsByBooker_withValidParamsAndWaitingState_returnsExpectedBookings() {
        Long bookerId = 1L;
        String state = "WAITING";
        BookingPaginationParams params = new BookingPaginationParams(0, 10);
        User user = new User(bookerId, "John Doe", "email@email.com");
        List<Booking> bookings = Arrays.asList(
                new Booking(1L, LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                        new Item(1L, "Item 1", "description 1", true, user), user, Status.WAITING),
                new Booking(2L, LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                        new Item(2L, "Item 2", "description 2", true, user), user, Status.WAITING),
                new Booking(3L, LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                        new Item(3L, "Item 3", "description3", true, user), user, Status.WAITING));
        Page<Booking> bookingPage = new PageImpl<>(bookings);
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(user));
        when(bookingRepository.findByStatusByBooker(Status.WAITING, bookerId, PageRequest.of(params.getFrom(), params.getSize())))
                .thenReturn(bookingPage);

        List<BookingResponseDto> result = bookingService.getBookingsByBooker(bookerId, state, params);

        assertEquals(bookings.size(), result.size());
        assertEquals(bookings.get(0).getId(), result.get(0).getId());
        assertEquals(bookings.get(1).getId(), result.get(1).getId());
        assertEquals(bookings.get(2).getId(), result.get(2).getId());
        verify(userRepository).findById(bookerId);
        verify(bookingRepository).findByStatusByBooker(Status.WAITING, bookerId, PageRequest.of(params.getFrom(), params.getSize()));
    }

    @Test
    public void getBookingsByBooker_withValidParamsAndRejectedState_returnsExpectedBookings() {
        Long bookerId = 1L;
        String state = "REJECTED";
        BookingPaginationParams params = new BookingPaginationParams(0, 10);
        User user = new User(bookerId, "John Doe", "email@email.com");
        List<Booking> bookings = Arrays.asList(
                new Booking(1L, LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                        new Item(1L, "Item 1", "description 1", true, user), user, Status.REJECTED),
                new Booking(2L, LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                        new Item(2L, "Item 2", "description 2", true, user), user, Status.REJECTED),
                new Booking(3L, LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                        new Item(3L, "Item 3", "description3", true, user), user, Status.REJECTED));
        Page<Booking> bookingPage = new PageImpl<>(bookings);
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(user));
        when(bookingRepository.findByStatusByBooker(Status.REJECTED, bookerId, PageRequest.of(params.getFrom(), params.getSize())))
                .thenReturn(bookingPage);

        List<BookingResponseDto> result = bookingService.getBookingsByBooker(bookerId, state, params);

        assertEquals(bookings.size(), result.size());
        assertEquals(bookings.get(0).getId(), result.get(0).getId());
        assertEquals(bookings.get(1).getId(), result.get(1).getId());
        assertEquals(bookings.get(2).getId(), result.get(2).getId());
        verify(userRepository).findById(bookerId);
        verify(bookingRepository).findByStatusByBooker(Status.REJECTED, bookerId, PageRequest.of(params.getFrom(), params.getSize()));
    }

    @Test
    public void getBookingsByBooker_withNonValidState_throwsException() {
        Long bookerId = 1L;
        String state = "UNSUPPORTED";
        BookingPaginationParams params = new BookingPaginationParams(0, 10);
        User user = new User(bookerId, "John Doe", "email@email.com");

        when(userRepository.findById(bookerId)).thenReturn(Optional.of(user));

        assertThrows(UnsupportedStateException.class,
                () -> bookingService.getBookingsByBooker(bookerId, state, params));

        verify(userRepository).findById(bookerId);
    }

    @Test
    public void getBookingsByOwner_withValidParamsAndAllState_returnsExpectedBookings() {
        Long ownerId = 1L;
        String state = "ALL";
        BookingPaginationParams params = new BookingPaginationParams(0, 10);
        User user = new User(ownerId, "John Doe", "email@email.com");
        List<Booking> bookings = Arrays.asList(
                new Booking(1L, LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                        new Item(1L, "Item 1", "description 1", true, user), user, Status.APPROVED),
                new Booking(2L, LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                        new Item(2L, "Item 2", "description 2", true, user), user, Status.REJECTED),
                new Booking(3L, LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                        new Item(3L, "Item 3", "description3", true, user), user, Status.WAITING));
        Page<Booking> bookingPage = new PageImpl<>(bookings);
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(user));
        when(bookingRepository.findAllByOwner(ownerId, PageRequest.of(params.getFrom(), params.getSize())))
                .thenReturn(bookingPage);

        List<BookingResponseDto> result = bookingService.getBookingsByOwner(ownerId, state, params);

        assertEquals(bookings.size(), result.size());
        assertEquals(bookings.get(0).getId(), result.get(0).getId());
        assertEquals(bookings.get(1).getId(), result.get(1).getId());
        assertEquals(bookings.get(2).getId(), result.get(2).getId());
        verify(userRepository).findById(ownerId);
        verify(bookingRepository).findAllByOwner(ownerId, PageRequest.of(params.getFrom(), params.getSize()));
    }

    @Test
    public void getBookingsByOwner_withInvalidUserId_throwsEntityNotFoundException() {
        Long bookerId = 1L;
        String state = "ALL";
        BookingPaginationParams params = new BookingPaginationParams(0, 10);
        when(userRepository.findById(bookerId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> bookingService.getBookingsByOwner(bookerId, state, params));
    }

    @Test
    public void getBookingsByOwner_withValidParamsAndFutureState_returnsExpectedBookings() {
        Long bookerId = 1L;
        String state = "FUTURE";
        BookingPaginationParams params = new BookingPaginationParams(0, 10);
        User user = new User(bookerId, "John Doe", "email@email.com");
        List<Booking> bookings = Arrays.asList(
                new Booking(1L, LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                        new Item(1L, "Item 1", "description 1", true, user), user, Status.APPROVED),
                new Booking(2L, LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                        new Item(2L, "Item 2", "description 2", true, user), user, Status.REJECTED),
                new Booking(3L, LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                        new Item(3L, "Item 3", "description3", true, user), user, Status.WAITING));
        Page<Booking> bookingPage = new PageImpl<>(bookings);
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(user));
        when(bookingRepository.findFutureBookingsByOwner(bookerId, PageRequest.of(params.getFrom(), params.getSize())))
                .thenReturn(bookingPage);

        List<BookingResponseDto> result = bookingService.getBookingsByOwner(bookerId, state, params);

        assertEquals(bookings.size(), result.size());
        assertEquals(bookings.get(0).getId(), result.get(0).getId());
        assertEquals(bookings.get(1).getId(), result.get(1).getId());
        assertEquals(bookings.get(2).getId(), result.get(2).getId());
        verify(userRepository).findById(bookerId);
        verify(bookingRepository).findFutureBookingsByOwner(bookerId, PageRequest.of(params.getFrom(), params.getSize()));
    }

    @Test
    public void getBookingsByOwner_withValidParamsAndPastState_returnsExpectedBookings() {
        Long bookerId = 1L;
        String state = "PAST";
        BookingPaginationParams params = new BookingPaginationParams(0, 10);
        User user = new User(bookerId, "John Doe", "email@email.com");
        List<Booking> bookings = Arrays.asList(
                new Booking(1L, LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                        new Item(1L, "Item 1", "description 1", true, user), user, Status.APPROVED),
                new Booking(2L, LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                        new Item(2L, "Item 2", "description 2", true, user), user, Status.REJECTED),
                new Booking(3L, LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                        new Item(3L, "Item 3", "description3", true, user), user, Status.WAITING));
        Page<Booking> bookingPage = new PageImpl<>(bookings);
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(user));
        when(bookingRepository.findPastBookingsByOwner(bookerId, PageRequest.of(params.getFrom(), params.getSize())))
                .thenReturn(bookingPage);

        List<BookingResponseDto> result = bookingService.getBookingsByOwner(bookerId, state, params);

        assertEquals(bookings.size(), result.size());
        assertEquals(bookings.get(0).getId(), result.get(0).getId());
        assertEquals(bookings.get(1).getId(), result.get(1).getId());
        assertEquals(bookings.get(2).getId(), result.get(2).getId());
        verify(userRepository).findById(bookerId);
        verify(bookingRepository).findPastBookingsByOwner(bookerId, PageRequest.of(params.getFrom(), params.getSize()));
    }

    @Test
    public void getBookingsByOwner_withValidParamsAndCurrentState_returnsExpectedBookings() {
        Long bookerId = 1L;
        String state = "CURRENT";
        BookingPaginationParams params = new BookingPaginationParams(0, 10);
        User user = new User(bookerId, "John Doe", "email@email.com");
        List<Booking> bookings = Arrays.asList(
                new Booking(1L, LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                        new Item(1L, "Item 1", "description 1", true, user), user, Status.APPROVED),
                new Booking(2L, LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                        new Item(2L, "Item 2", "description 2", true, user), user, Status.REJECTED),
                new Booking(3L, LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                        new Item(3L, "Item 3", "description3", true, user), user, Status.WAITING));
        Page<Booking> bookingPage = new PageImpl<>(bookings);
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(user));
        when(bookingRepository.findCurrentBookingsByOwner(bookerId, PageRequest.of(params.getFrom(), params.getSize())))
                .thenReturn(bookingPage);

        List<BookingResponseDto> result = bookingService.getBookingsByOwner(bookerId, state, params);

        assertEquals(bookings.size(), result.size());
        assertEquals(bookings.get(0).getId(), result.get(0).getId());
        assertEquals(bookings.get(1).getId(), result.get(1).getId());
        assertEquals(bookings.get(2).getId(), result.get(2).getId());
        verify(userRepository).findById(bookerId);
        verify(bookingRepository).findCurrentBookingsByOwner(bookerId, PageRequest.of(params.getFrom(), params.getSize()));
    }

    @Test
    public void getBookingsByOwner_withValidParamsAndWaitingState_returnsExpectedBookings() {
        Long bookerId = 1L;
        String state = "WAITING";
        BookingPaginationParams params = new BookingPaginationParams(0, 10);
        User user = new User(bookerId, "John Doe", "email@email.com");
        List<Booking> bookings = Arrays.asList(
                new Booking(1L, LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                        new Item(1L, "Item 1", "description 1", true, user), user, Status.WAITING),
                new Booking(2L, LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                        new Item(2L, "Item 2", "description 2", true, user), user, Status.WAITING),
                new Booking(3L, LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                        new Item(3L, "Item 3", "description3", true, user), user, Status.WAITING));
        Page<Booking> bookingPage = new PageImpl<>(bookings);
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(user));
        when(bookingRepository.findByStatusByOwner(Status.WAITING, bookerId, PageRequest.of(params.getFrom(), params.getSize())))
                .thenReturn(bookingPage);

        List<BookingResponseDto> result = bookingService.getBookingsByOwner(bookerId, state, params);

        assertEquals(bookings.size(), result.size());
        assertEquals(bookings.get(0).getId(), result.get(0).getId());
        assertEquals(bookings.get(1).getId(), result.get(1).getId());
        assertEquals(bookings.get(2).getId(), result.get(2).getId());
        verify(userRepository).findById(bookerId);
        verify(bookingRepository).findByStatusByOwner(Status.WAITING, bookerId, PageRequest.of(params.getFrom(), params.getSize()));
    }

    @Test
    public void getBookingsByOwner_withValidParamsAndRejectedState_returnsExpectedBookings() {
        Long bookerId = 1L;
        String state = "REJECTED";
        BookingPaginationParams params = new BookingPaginationParams(0, 10);
        User user = new User(bookerId, "John Doe", "email@email.com");
        List<Booking> bookings = Arrays.asList(
                new Booking(1L, LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                        new Item(1L, "Item 1", "description 1", true, user), user, Status.REJECTED),
                new Booking(2L, LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                        new Item(2L, "Item 2", "description 2", true, user), user, Status.REJECTED),
                new Booking(3L, LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                        new Item(3L, "Item 3", "description3", true, user), user, Status.REJECTED));
        Page<Booking> bookingPage = new PageImpl<>(bookings);
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(user));
        when(bookingRepository.findByStatusByOwner(Status.REJECTED, bookerId, PageRequest.of(params.getFrom(), params.getSize())))
                .thenReturn(bookingPage);

        List<BookingResponseDto> result = bookingService.getBookingsByOwner(bookerId, state, params);

        assertEquals(bookings.size(), result.size());
        assertEquals(bookings.get(0).getId(), result.get(0).getId());
        assertEquals(bookings.get(1).getId(), result.get(1).getId());
        assertEquals(bookings.get(2).getId(), result.get(2).getId());
        verify(userRepository).findById(bookerId);
        verify(bookingRepository).findByStatusByOwner(Status.REJECTED, bookerId, PageRequest.of(params.getFrom(), params.getSize()));
    }

    @Test
    public void getBookingsByOwner_withNonValidState_throwsException() {
        Long bookerId = 1L;
        String state = "UNSUPPORTED";
        BookingPaginationParams params = new BookingPaginationParams(0, 10);
        User user = new User(bookerId, "John Doe", "email@email.com");
       
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(user));

        assertThrows(UnsupportedStateException.class,
                () -> bookingService.getBookingsByOwner(bookerId, state, params));

        verify(userRepository).findById(bookerId);
    }
}