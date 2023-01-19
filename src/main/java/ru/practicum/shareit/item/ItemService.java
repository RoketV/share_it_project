package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
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
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;


    public ItemResponseDto addItem(ItemRequestDto dto, long userId) {

        if (userRepository.findById(userId).isEmpty()) {
            throw new EntityNotFoundException(String.format("there is no user with id %d for new item", userId));
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Item has to belong to user." +
                        "User with id %d not found", userId)));
        dto.setUser(user);
        Item item = ItemMapper.ITEM_MAPPER.toItem(dto);
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
        return ItemMapper.ITEM_MAPPER.toDto(itemRepository.save(item));
    }

    public ItemResponseDto getItem(Long itemId) {
//        Item item = itemRepository.findById(itemId)
//                .orElseThrow(() -> new EntityNotFoundException(String.format("item with id %s not found", itemId)));
//        List<Booking> bookings = bookingRepository.findAllByItem_Id(itemId);
//        LocalDateTime now = LocalDateTime.now();
//        for (Booking booking : bookings) {
//
//            if (booking.getEnd().)
//        }
        return ItemMapper.ITEM_MAPPER.toDto(itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("item with id %s not found", itemId))));
    }

    public Set<ItemResponseDto> getItems(Long userId) {
        return itemRepository.findAll()
                .stream()
                .filter(item -> item.getUser().getId() == userId)
                .map(ItemMapper.ITEM_MAPPER::toDto)
                .sorted(Comparator.comparing(ItemResponseDto::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Transactional
    public ItemResponseDto deleteItem(Long id) {
        Optional<Item> item = itemRepository.findById(id);
        item.ifPresent(itemRepository::delete);
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

    private boolean userIsTheSame(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("cannot find Item with %s id", itemId)));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("cannot find User with %s id", userId)));
        return item.getUser().equals(user);
    }
}
