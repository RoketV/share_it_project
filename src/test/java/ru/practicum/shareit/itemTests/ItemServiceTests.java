package ru.practicum.shareit.itemTests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.comments.Comment;
import ru.practicum.shareit.comments.CommentRepository;
import ru.practicum.shareit.comments.dto.CommentInputDto;
import ru.practicum.shareit.comments.dto.CommentMapper;
import ru.practicum.shareit.comments.dto.CommentOutputDto;
import ru.practicum.shareit.exceptions.CommentConsistencyException;
import ru.practicum.shareit.exceptions.EntityNotFoundException;
import ru.practicum.shareit.item.ItemPaginationParams;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemInputDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.dto.ItemOutputDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ActiveProfiles("test")
@Transactional
public class ItemServiceTests {

    @MockBean
    private ItemRepository itemRepository;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private BookingRepository bookingRepository;
    @MockBean
    private CommentRepository commentRepository;
    @MockBean
    private ItemRequestRepository requestRepository;
    @MockBean
    private ItemService itemService;


    @BeforeEach
    void setUp() {
        itemService = new ItemService(itemRepository, userRepository, bookingRepository, commentRepository,
                requestRepository);
    }

    @Test
    void addItem_WhenUserNotFound_ShouldThrowException() {
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> itemService.addItem(new ItemInputDto(), 1L));
    }

    @Test
    void updateItem_WhenUserNotFound_ShouldThrowException() {
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> itemService.updateItem(new ItemInputDto(), 1L, 1L));
    }

    @Test
    void getItem_WhenItemNotFound_ShouldThrowException() {
        when(itemRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> itemService.getItem(1L, 1L));
    }

    @Test
    void searchItem_WhenTextIsBlank_ShouldReturnEmptyList() {
        when(itemRepository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

        List<ItemOutputDto> result = itemService.searchItem("", new ItemPaginationParams(1, 20));

        assertEquals(0, result.size());
    }

    @Test
    public void addItem_shouldAddItem() {
        ItemInputDto inputDto = new ItemInputDto();
        inputDto.setName("Test Item");
        inputDto.setAvailable(true);
        inputDto.setDescription("A test item");

        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(1L);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(itemRequest));

        Item item = ItemMapper.ITEM_MAPPER.toItem(inputDto);

        when(itemRepository.save(any())).thenReturn(item);

        ItemOutputDto outputDto = itemService.addItem(inputDto, 1L);

        Assertions.assertAll(
                () -> assertNotNull(outputDto),
                () -> assertEquals("Test Item", outputDto.getName()),
                () -> assertTrue(outputDto.getAvailable()),
                () -> assertEquals("A test item", outputDto.getDescription())
        );
    }

    @Test
    public void updateItem_shouldUpdateItem() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ItemInputDto inputDto = new ItemInputDto();
        inputDto.setName("Updated Item");
        inputDto.setAvailable(false);
        inputDto.setDescription("An updated item");
        inputDto.setUser(user);

        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setAvailable(true);
        item.setDescription("A test item");
        item.setUser(user);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        when(itemRepository.save(item)).thenReturn(item);

        ItemOutputDto outputDto = itemService.updateItem(inputDto, 1L, 1L);

        Assertions.assertAll(
                () -> assertNotNull(outputDto),
                () -> assertEquals("Updated Item", outputDto.getName()),
                () -> assertFalse(outputDto.getAvailable()),
                () -> assertEquals("An updated item", outputDto.getDescription())
        );
    }

    @Test
    public void getItem_returnsItemOutputDto() {
        Long ownerId = 1L;
        Long itemId = 1L;

        User owner = new User();
        owner.setId(ownerId);

        Item item = new Item();
        item.setId(itemId);
        item.setName("Test Item");
        item.setDescription("A test item");
        item.setUser(owner);

        Booking pastBooking = new Booking();
        pastBooking.setId(1L);
        pastBooking.setItem(item);
        pastBooking.setStart(LocalDateTime.now().minusDays(2));
        pastBooking.setEnd(LocalDateTime.now().minusDays(1));
        pastBooking.setUser(new User());

        Booking futureBooking = new Booking();
        futureBooking.setId(2L);
        futureBooking.setItem(item);
        futureBooking.setStart(LocalDateTime.now().plusDays(1));
        futureBooking.setEnd(LocalDateTime.now().plusDays(2));
        futureBooking.setUser(new User());

        Comment comment1 = new Comment();
        comment1.setId(1L);
        comment1.setItem(item);
        comment1.setText("Comment 1");
        comment1.setAuthor(new User());

        Comment comment2 = new Comment();
        comment2.setId(2L);
        comment2.setItem(item);
        comment2.setText("Comment 2");
        comment2.setAuthor(new User());

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.findAllByItem_Id(itemId)).thenReturn(Arrays.asList(pastBooking, futureBooking));
        when(commentRepository.findAllByItem_Id(itemId)).thenReturn(Arrays.asList(comment1, comment2));

        ItemOutputDto outputDto = itemService.getItem(ownerId, itemId);

        Assertions.assertAll(
                () -> assertEquals(itemId, outputDto.getId()),
                () -> assertEquals("Test Item", outputDto.getName()),
                () -> assertEquals("A test item", outputDto.getDescription())
        );

        List<CommentOutputDto> comments = outputDto.getComments();
        Assertions.assertAll(
                () -> assertEquals(2, comments.size()),
                () -> assertEquals(1L, comments.get(0).getId().longValue()),
                () -> assertEquals("Comment 1", comments.get(0).getText()),
                () -> assertEquals(2L, comments.get(1).getId().longValue()),
                () -> assertEquals("Comment 2", comments.get(1).getText())
        );
    }

    @Test
    public void testGetItems() {
        ItemPaginationParams params = new ItemPaginationParams(0, 10);

        List<Item> items = Arrays.asList(new Item(), new Item());
        Page<Item> itemsPage = new PageImpl<>(items);
        when(itemRepository.findAllByUser_IdOrderByIdAsc(any(), eq(PageRequest.of(params.getFrom(), params.getSize()))))
                .thenReturn(itemsPage);

        List<ItemOutputDto> getItems = itemService.getItems(1L, params);

        Assertions.assertAll(
                () -> assertNotNull(getItems),
                () -> assertEquals(2, getItems.size())
        );
    }

    @Test
    @Transactional
    public void deleteItem_ItemExists_ItemDeleted() {
        Item item = new Item();
        item.setName("Test Item");
        item.setDescription("Test description");
        item.setAvailable(true);
        item.setUser(new User());

        when(itemRepository.findById(any())).thenReturn(Optional.of(item));

        ItemOutputDto deletedItem = itemService.deleteItem(item.getId());

        Assertions.assertAll(
                () -> assertNotNull(deletedItem),
                () -> assertEquals((deletedItem.getName()), item.getName()),
                () -> assertEquals(deletedItem.getDescription(), item.getDescription()),
                () -> assertEquals(deletedItem.getAvailable(), item.getAvailable()),
                () -> assertEquals(deletedItem.getUser().getId(), item.getUser().getId())
        );

        verify(itemRepository, times(1)).findById(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void searchItem_withBlankText_shouldReturnEmptyList(String string) {
        ItemPaginationParams params = new ItemPaginationParams(0, 10);

        List<ItemOutputDto> result = itemService.searchItem(string, params);

        assertEquals(Collections.emptyList(), result);
    }

    @Test
    void searchItem_withNonBlankText_shouldReturnMatchingItems() {
        Item item1 = new Item();
        item1.setId(1L);
        item1.setName("Test Item 1");
        item1.setDescription("Test description 1");
        item1.setAvailable(true);
        item1.setUser(new User());

        Item item2 = new Item();
        item2.setId(2L);
        item2.setName("Another Item");
        item2.setDescription("Another description");
        item2.setAvailable(true);
        item2.setUser(new User());

        Item item3 = new Item();
        item3.setId(3L);
        item3.setName("Test Item 2");
        item3.setDescription("Test description 2");
        item3.setAvailable(true);
        item3.setUser(new User());

        String text = "test";
        ItemPaginationParams params = new ItemPaginationParams(0, 10);
        List<Item> items = List.of(item1, item2, item3);
        Page<Item> page = new PageImpl<>(items);

        when(itemRepository.findAll(any(Pageable.class))).thenReturn(page);

        List<ItemOutputDto> result = itemService.searchItem(text, params);

        Assertions.assertAll(
                () -> assertEquals(2, result.size()),
                () -> assertEquals("Test Item 1", result.get(0).getName()),
                () -> assertEquals("Test description 1", result.get(0).getDescription()),
                () -> assertEquals("Test Item 2", result.get(1).getName()),
                () -> assertEquals("Test description 2", result.get(1).getDescription())
        );

        verify(itemRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void searchItem_withNonBlankText_andNoMatchingItems_shouldThrowEntityNotFoundException() {
        String text = "test";
        ItemPaginationParams params = new ItemPaginationParams(0, 10);
        Page<Item> page = new PageImpl<>(Collections.emptyList());

        when(itemRepository.findAll(any(Pageable.class))).thenReturn(page);

        assertThrows(EntityNotFoundException.class, () -> itemService.searchItem(text, params));
    }

    @Test
    void addComment_IfItemNotFound_ShouldThrowException() {
        Long itemId = 1L;
        Long userId = 1L;
        CommentInputDto inputDto = new CommentInputDto();
        inputDto.setText("test comment");

        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class,
                () -> itemService.addComment(inputDto, itemId, userId));
    }

    @Test
    void addComment_IfUserNotFound_ShouldThrowException() {
        Long itemId = 1L;
        Long userId = 1L;
        CommentInputDto inputDto = new CommentInputDto();
        inputDto.setText("test comment");

        Item item = new Item();
        item.setId(itemId);

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        Assertions.assertThrows(CommentConsistencyException.class,
                () -> itemService.addComment(inputDto, itemId, userId));
    }

    @Test
    void addCommentShouldThrowExceptionIfNoPastBookingsFound() {
        Long itemId = 1L;
        Long userId = 1L;
        CommentInputDto inputDto = new CommentInputDto();
        inputDto.setText("test comment");

        Item item = new Item();
        item.setId(itemId);

        User user = new User();
        user.setId(userId);

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(bookingRepository.findPastBookingsByBookerAndItem(anyLong(), anyLong())).thenReturn(new ArrayList<>());

        Assertions.assertThrows(CommentConsistencyException.class,
                () -> itemService.addComment(inputDto, itemId, userId)
        );
    }

    @Test
    void addComment_ShouldCreateCommentAndReturnDto() {
        CommentInputDto inputDto = new CommentInputDto();
        inputDto.setText("This is a test comment");


        User user = new User(2L, "creator name", "email@email.com");
        Item item = new Item(1L, "item name", "item description", true, user);


        List<Booking> pastBookings = new ArrayList<>();
        pastBookings.add(new Booking());
        Comment comment = CommentMapper.COMMENT_MAPPER.toComment(inputDto);
        comment.setAuthor(user);
        comment.setCreated(LocalDateTime.now());
        comment.setItem(item);
        comment.setId(1L);

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingRepository.findPastBookingsByBookerAndItem(user.getId(), item.getId())).thenReturn(pastBookings);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(commentRepository.save(any())).thenReturn(comment);

        CommentOutputDto result = itemService.addComment(inputDto, item.getId(), user.getId());

        assertAll(
                () -> assertNotNull(result),
                () -> assertNotNull(result.getId()),
                () -> assertEquals(inputDto.getText(), result.getText()),
                () -> assertEquals(item.getId(), result.getId()),
                () -> assertEquals(user.getName(), result.getAuthorName())
        );
    }
}
