package ru.practicum.shareit.bookingTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.BookingPaginationParams;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
public class BookingControllerTests {

    @Autowired
    private MockMvc mockMvc;
    private ObjectMapper mapper;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS");
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    @BeforeEach
    public void setUp() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .setDateFormat(dateFormat);
    }

    @MockBean
    private BookingService bookingService;

    @Test
    public void createBooking_shouldReturnBookingResponseDto() throws Exception {
        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setItemId(2L);
        requestDto.setStart(LocalDateTime.now().plusHours(1));
        requestDto.setEnd(LocalDateTime.now().plusDays(1));

        BookingResponseDto responseDto = new BookingResponseDto();
        responseDto.setId(1L);
        responseDto.setUser(new User(1L, "name", "email@email.com"));
        responseDto.setStart(LocalDateTime.now());
        responseDto.setEnd(LocalDateTime.now().plusDays(1));
        responseDto.setStatus(Status.APPROVED);

        when(bookingService.createBooking(any(BookingRequestDto.class), anyLong())).thenReturn(responseDto);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()))
                .andExpect(jsonPath("$.booker.id").value(responseDto.getUser().getId()))
                .andExpect(jsonPath("$.end").value(responseDto.getEnd().format(formatter)))
                .andExpect(jsonPath("$.status").value(responseDto.getStatus().toString()));
    }

    @Test
    public void patchBooking_shouldReturnBookingResponseDto() throws Exception {
        Long bookingId = 1L;
        Long userId = 2L;
        Boolean approved = true;

        BookingResponseDto responseDto = new BookingResponseDto();
        responseDto.setId(bookingId);
        responseDto.setUser(new User(userId, "name", "email@email.com"));
        responseDto.setStart(LocalDateTime.now());
        responseDto.setEnd(LocalDateTime.now().plusDays(1));
        responseDto.setStatus(Status.APPROVED);

        when(bookingService.approveBooking(eq(bookingId), eq(userId), eq(approved))).thenReturn(responseDto);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId)
                        .param("approved", approved.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()))
                .andExpect(jsonPath("$.booker.id").value(responseDto.getUser().getId()))
                .andExpect(jsonPath("$.start").value(responseDto.getStart().format(formatter)))
                .andExpect(jsonPath("$.end").value(responseDto.getEnd().format(formatter)))
                .andExpect(jsonPath("$.status").value(responseDto.getStatus().toString()));
    }

    @Test
    public void getBooking_shouldReturnBookingResponseDto() throws Exception {
        Long bookingId = 1L;
        Long userId = 2L;

        BookingResponseDto responseDto = new BookingResponseDto();
        responseDto.setId(bookingId);
        responseDto.setUser(new User(userId, "name", "email@email.com"));
        responseDto.setStart(LocalDateTime.now());
        responseDto.setEnd(LocalDateTime.now().plusDays(1));
        responseDto.setStatus(Status.APPROVED);

        when(bookingService.getBooking(eq(bookingId), eq(userId))).thenReturn(responseDto);

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()))
                .andExpect(jsonPath("$.booker.id").value(responseDto.getUser().getId()))
                .andExpect(jsonPath("$.start").value(responseDto.getStart().format(formatter)))
                .andExpect(jsonPath("$.end").value(responseDto.getEnd().format(formatter)))
                .andExpect(jsonPath("$.status").value(responseDto.getStatus().toString()));
    }

    @Test
    public void getBookingsByBooker_shouldReturnBookingResponseDto() throws Exception {
        Long bookingId1 = 1L;
        Long bookingId2 = 2L;
        Long userId = 2L;
        String state = "ALL";

        User user = new User(userId, "name", "email@email.com");


        BookingResponseDto responseDto1 = new BookingResponseDto();
        responseDto1.setId(bookingId1);
        responseDto1.setUser(user);
        responseDto1.setStart(LocalDateTime.now());
        responseDto1.setEnd(LocalDateTime.now().plusDays(1));
        responseDto1.setStatus(Status.APPROVED);

        BookingResponseDto responseDto2 = new BookingResponseDto();
        responseDto2.setId(bookingId2);
        responseDto2.setUser(user);
        responseDto2.setStart(LocalDateTime.now());
        responseDto2.setEnd(LocalDateTime.now().plusDays(1));
        responseDto2.setStatus(Status.APPROVED);

        List<BookingResponseDto> responseDtos = Arrays.asList(responseDto1, responseDto2);
        BookingPaginationParams params = new BookingPaginationParams(0, 20);

        when(bookingService.getBookingsByBooker(
                eq(userId), eq(state), eq(params))).thenReturn(responseDtos);

        mockMvc.perform(get("/bookings/")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", state)
                        .param("from", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(responseDto1.getId()))
                .andExpect(jsonPath("$[0].booker.id").value(responseDto1.getUser().getId()))
                .andExpect(jsonPath("$[0].start").value(responseDto1.getStart().format(formatter)))
                .andExpect(jsonPath("$[0].end").value(responseDto1.getEnd().format(formatter)))
                .andExpect(jsonPath("$[0].status").value(responseDto1.getStatus().toString()))
                .andExpect(jsonPath("$[1].id").value(responseDto2.getId()))
                .andExpect(jsonPath("$[1].booker.id").value(responseDto2.getUser().getId()))
                .andExpect(jsonPath("$[1].start").value(responseDto2.getStart().format(formatter)))
                .andExpect(jsonPath("$[1].end").value(responseDto2.getEnd().format(formatter)))
                .andExpect(jsonPath("$[1].status").value(responseDto2.getStatus().toString()));
    }

    @Test
    public void getBookingsByOwner_shouldReturnBookingResponseDto() throws Exception {
        Long bookingId1 = 1L;
        Long bookingId2 = 2L;
        Long userId = 2L;
        Long ownerId = 3L;
        String state = "ALL";

        User user = new User(userId, "name", "email@email.com");
        User owner = new User(ownerId, "owner name", "owneremail@email.com");

        Item item = new Item(1L, "item name", "item description", true, owner);

        BookingResponseDto responseDto1 = new BookingResponseDto();
        responseDto1.setId(bookingId1);
        responseDto1.setItem(item);
        responseDto1.setUser(user);
        responseDto1.setStart(LocalDateTime.now());
        responseDto1.setEnd(LocalDateTime.now().plusDays(1));
        responseDto1.setStatus(Status.APPROVED);

        BookingResponseDto responseDto2 = new BookingResponseDto();
        responseDto2.setId(bookingId2);
        responseDto2.setItem(item);
        responseDto2.setUser(user);
        responseDto2.setStart(LocalDateTime.now());
        responseDto2.setEnd(LocalDateTime.now().plusDays(1));
        responseDto2.setStatus(Status.APPROVED);

        List<BookingResponseDto> responseDtos = Arrays.asList(responseDto1, responseDto2);
        BookingPaginationParams params = new BookingPaginationParams(0, 20);

        when(bookingService.getBookingsByOwner(
                eq(userId), eq(state), eq(params))).thenReturn(responseDtos);

        mockMvc.perform(get("/bookings/owner/")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", state)
                        .param("from", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(responseDto1.getId()))
                .andExpect(jsonPath("$[0].booker.id").value(responseDto1.getUser().getId()))
                .andExpect(jsonPath("$[0].start").value(responseDto1.getStart().format(formatter)))
                .andExpect(jsonPath("$[0].end").value(responseDto1.getEnd().format(formatter)))
                .andExpect(jsonPath("$[0].status").value(responseDto1.getStatus().toString()))
                .andExpect(jsonPath("$[1].id").value(responseDto2.getId()))
                .andExpect(jsonPath("$[1].booker.id").value(responseDto2.getUser().getId()))
                .andExpect(jsonPath("$[1].start").value(responseDto2.getStart().format(formatter)))
                .andExpect(jsonPath("$[1].end").value(responseDto2.getEnd().format(formatter)))
                .andExpect(jsonPath("$[1].status").value(responseDto2.getStatus().toString()));
    }
}
