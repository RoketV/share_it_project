package ru.practicum.shareit.itemRequestTests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.exceptions.EntityNotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemOutputDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestPaginationParams;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestInputDto;
import ru.practicum.shareit.request.dto.ItemRequestOutputDto;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceTests {

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemRequestService itemRequestService;

    @Test
    public void testAddRequest() {
        Long userId = 1L;
        ItemRequestInputDto inputDto = new ItemRequestInputDto();
        User owner = new User(userId, "owner name", "email@emai.com");
        inputDto.setOwner(owner);
        ItemRequest request = new ItemRequest(1L, "description", LocalDateTime.now(), owner);

        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
        when(itemRequestRepository.save(Mockito.any(ItemRequest.class))).thenReturn(request);

        ItemRequestOutputDto outputDto = itemRequestService.addRequest(inputDto, userId);

        assertAll(
                () -> assertNotNull(outputDto),
                () -> assertEquals(1L, outputDto.getId())
        );

        Mockito.verify(userRepository, Mockito.times(2)).findById(userId);
        Mockito.verify(itemRequestRepository, Mockito.times(1)).save(Mockito.any(ItemRequest.class));
    }

    @Test
    public void testAddRequestWithInvalidUser() {
        Long userId = 1L;
        ItemRequestInputDto inputDto = new ItemRequestInputDto();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> itemRequestService.addRequest(inputDto, userId));
    }

    @Test
    public void testGetItemRequestWithItemsResponse() {
        Long userId = 1L;
        LocalDateTime now = LocalDateTime.now();
        User user = new User(userId, "User1", "user1@example.com");
        ItemRequest request1 = new ItemRequest(1L, "Request1", now, user);
        ItemRequest request2 = new ItemRequest(2L, "Request2", now, user);
        List<ItemRequest> itemRequests = Arrays.asList(request1, request2);

        List<ItemRequestOutputDto> expectedOutput = new ArrayList<>();
        expectedOutput.add(new ItemRequestOutputDto(1L, "Request1", now, Collections.emptyList()));
        expectedOutput.add(new ItemRequestOutputDto(2L, "Request2", now, Collections.emptyList()));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findAllByUser_Id(userId)).thenReturn(itemRequests);

        List<ItemRequestOutputDto> result = itemRequestService.getItemRequestWithItemsResponse(userId);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(2, result.size()),
                () -> assertEquals(expectedOutput.get(0), result.get(0)),
                () -> assertEquals(expectedOutput.get(1), result.get(1))
        );
    }

    @Test
    public void getItemRequest_shouldReturnCorrectOutputDto() {
        Long itemId = 1L;
        Long userId = 2L;

        User user = new User(userId, "Test User", "testuser@example.com");
        ItemRequest itemRequest = new ItemRequest(itemId, "Test Item Request", LocalDateTime.now(), user);

        Item item1 = new Item(1L, "Item 1", "description 1", true, user, itemRequest);
        Item item2 = new Item(2L, "Item 2", "description 2", true, user, itemRequest);

        List<Item> items = Arrays.asList(item1, item2);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(itemId)).thenReturn(Optional.of(itemRequest));
        when(itemRepository.findItemsByRequestId(itemId)).thenReturn(items);
        ItemRequestOutputDto result = itemRequestService.getItemRequest(itemId, userId);

        List<ItemOutputDto> itemDtos = result.getItems();
        assertAll(
                () -> Assertions.assertEquals(itemId, result.getId()),
                () -> Assertions.assertEquals(2, itemDtos.size()),
                () -> Assertions.assertEquals(1L, itemDtos.get(0).getId()),
                () -> Assertions.assertEquals("Item 1", itemDtos.get(0).getName()),
                () -> Assertions.assertEquals(2L, itemDtos.get(1).getId()),
                () -> Assertions.assertEquals("Item 2", itemDtos.get(1).getName())
        );
    }

    @Test
    void testGetItemRequestWithPagination() {
        Long userId = 1L;
        int page = 0;
        int size = 2;
        User user = new User(userId, "Test User", "testuser@example.com");
        ItemRequest itemRequest1 = new ItemRequest(1L, "Test Item Request 1", LocalDateTime.now(), user);
        ItemRequest itemRequest2 = new ItemRequest(2L, "Test Item Request 2", LocalDateTime.now(), user);
        List<ItemRequest> itemRequests = Arrays.asList(itemRequest1, itemRequest2);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findAllExceptUser(userId, PageRequest.of(page, size))).thenReturn(new PageImpl<>(itemRequests));

        List<ItemRequestOutputDto> result = itemRequestService.getItemRequestWithPagination(userId, new ItemRequestPaginationParams(page, size));

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(2, result.size()),
                () -> assertEquals(itemRequest1.getId(), result.get(0).getId()),
                () -> assertEquals(itemRequest2.getId(), result.get(1).getId())
        );
    }
}