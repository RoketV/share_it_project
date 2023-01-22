package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingInItemResponseDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.comments.Comment;
import ru.practicum.shareit.comments.CommentRepository;
import ru.practicum.shareit.comments.dto.CommentMapper;
import ru.practicum.shareit.comments.dto.CommentRequestDto;
import ru.practicum.shareit.comments.dto.CommentResponseDto;
import ru.practicum.shareit.exceptions.CommentConsistencyException;
import ru.practicum.shareit.exceptions.DifferentUsersException;
import ru.practicum.shareit.exceptions.EntityNotFoundException;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;


    @Transactional
    public ItemResponseDto addItem(ItemRequestDto dto, long userId) {

        if (userRepository.findById(userId).isEmpty()) {
            throw new EntityNotFoundException(String.format("there is no user with id %d for new item", userId));
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Item has to belong to user." +
                        "User with id %d not found", userId)));
        dto.setUser(user);
        Item item = ItemMapper.ITEM_MAPPER.toItem(dto);
        log.info("item with name {} added", item.getName());
        return ItemMapper.ITEM_MAPPER.toDto(itemRepository.save(item));
    }

    @Transactional
    public ItemResponseDto updateItem(ItemRequestDto dto, Long userId, Long itemId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new EntityNotFoundException(String.format("there is no user with id %d for new item", userId));
        }
        if (!userIsTheSame(itemId, userId)) {
            throw new DifferentUsersException("cannot update item's user");
        }
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("item with %d id not found", itemId)));
        if (dto.getName() != null) {
            item.setName(dto.getName());
        }
        if (dto.getUser() != null) {
            item.setUser(dto.getUser());
        }
        if (dto.getAvailable() != null) {
            item.setAvailable(dto.getAvailable());
        }
        if (dto.getDescription() != null) {
            item.setDescription(dto.getDescription());
        }
        log.info("item with id {} updated", itemId);
        return ItemMapper.ITEM_MAPPER.toDto(itemRepository.save(item));
    }

    public ItemResponseDto getItem(Long ownerId, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("item with id %s not found", itemId)));
        List<Booking> bookings = bookingRepository.findAllByItem_Id(itemId);
        ItemResponseDto dto = ItemMapper.ITEM_MAPPER.toDto(item);
        List<CommentResponseDto> comments = commentRepository.findAllByItem_Id(itemId)
                .stream()
                .map(CommentMapper.COMMENT_MAPPER::toDto)
                .collect(Collectors.toList());
        dto.setComments(comments);
        if (!item.getUser().getId().equals(ownerId)) {
            return dto;
        }
        LocalDateTime now = LocalDateTime.now();
        for (Booking booking : bookings) {
            if (booking.getEnd().isBefore(now)) {
                dto.setLastBooking(BookingMapper.BOOKING_MAPPER.toBookingInItemDto(booking));
            }
            if (booking.getStart().isAfter(now)) {
                dto.setNextBooking(BookingMapper.BOOKING_MAPPER.toBookingInItemDto(booking));
            }
        }
        return dto;
    }

    public List<ItemResponseDto> getItems(Long ownerId) {
        List<ItemResponseDto> items = itemRepository.findAllByUser_Id(ownerId)
                .stream()
                .map(ItemMapper.ITEM_MAPPER::toDto)
                .sorted(Comparator.comparing(ItemResponseDto::getId))
                .collect(Collectors.toList());
        List<BookingInItemResponseDto> bookings = bookingRepository.findAllByItem_User_Id(ownerId)
                .stream()
                .map(BookingMapper.BOOKING_MAPPER::toBookingInItemDto)
                .collect(Collectors.toList());

        for (ItemResponseDto item : items) {
            item.setLastBooking(bookings
                    .stream()
                    .filter(b -> b.getItemId().equals(item.getId()))
                    .filter(b -> b.getEnd().isBefore(LocalDateTime.now()))
                    .max(Comparator.comparing(BookingInItemResponseDto::getEnd))
                    .orElse(null));
            item.setNextBooking(bookings
                    .stream()
                    .filter(b -> b.getItemId().equals(item.getId()))
                    .filter(b -> b.getStart().isAfter(LocalDateTime.now()))
                    .min(Comparator.comparing(BookingInItemResponseDto::getStart))
                    .orElse(null));
        }
        return items;
    }

    @Transactional
    public ItemResponseDto deleteItem(Long id) {
        Optional<Item> item = itemRepository.findById(id);
        item.ifPresent(itemRepository::delete);
        log.info("item with id {} deleted", id);
        return ItemMapper.ITEM_MAPPER.toDto(item
                .orElseThrow(() -> new EntityNotFoundException("there is no such item to delete")));
    }

    public List<ItemResponseDto> searchItem(String text) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        return Optional.of(itemRepository.findAll()
                        .stream()
                        .filter(item -> item.getAvailable() &&
                                (item.getName().toLowerCase().contains(text.toLowerCase())
                                        || item.getDescription().toLowerCase().contains(text.toLowerCase())))
                        .map(ItemMapper.ITEM_MAPPER::toDto)
                        .collect(Collectors.toList()))
                .orElseThrow(() -> new EntityNotFoundException("there is no item which satisfies your search"));
    }

    public CommentResponseDto addComment(CommentRequestDto dto, Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("item with id %s not found", itemId)));
        LocalDateTime now = LocalDateTime.now();
        List<Booking> pastBookings = bookingRepository
                .findPastBookingsByBookerAndItem(now, userId, itemId);
        if (pastBookings.isEmpty()) {
            throw new CommentConsistencyException(String.format("user with id %d cannot leave comment for booking " +
                    "which is still current or in future", userId));
        }
        Comment comment = CommentMapper.COMMENT_MAPPER.toComment(dto);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Item has to belong to user." +
                        "User with id %d not found", userId)));
        comment.setItem(item);
        comment.setAuthor(user);
        comment.setCreated(now);
        log.info("comment added be user with id {}", userId);
        return CommentMapper.COMMENT_MAPPER.toDto(commentRepository.save(comment));
    }

    private boolean userIsTheSame(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("cannot find Item with %s id", itemId)));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("cannot find User with %s id", userId)));
        return item.getUser().equals(user);
    }
}
