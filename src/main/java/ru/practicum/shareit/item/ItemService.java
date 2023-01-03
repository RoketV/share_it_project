package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.DifferentUsersException;
import ru.practicum.shareit.exceptions.EntityNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public ItemDto addItem(ItemDto dto, long userId) {
        if (!userDoesExist(userId)) {
            throw new EntityNotFoundException(String.format("there is no item belongs to user with %s id", userId));
        }
        User user = userRepository.getUser(userId)
                .orElseThrow(() -> new EntityNotFoundException("cannot find user while adding Item"));
        dto.setUser(user);
        return ItemMapper.ITEM_MAPPER.toDto(itemRepository.addItem(ItemMapper.ITEM_MAPPER.toItem(dto))
                .orElseThrow(() -> new ValidationException("cannot add this item")));
    }

    public ItemDto updateItem(ItemDto dto, long userId, long itemId) {
        if (!userDoesExist(userId)) {
            throw new EntityNotFoundException(String.format("there is no item belongs to user with %s id", userId));
        }
        if (!userIsTheSame(itemId, userId)) {
            throw new DifferentUsersException("cannot update item's user");
        }
        dto.setId(itemId);
        return ItemMapper.ITEM_MAPPER.toDto(itemRepository.updateItem(ItemMapper.ITEM_MAPPER.toItem(dto))
                .orElseThrow(() -> new ValidationException("cannot update this item")));
    }

    public ItemDto getItem(long id) {
        return ItemMapper.ITEM_MAPPER.toDto(itemRepository.getItem(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("item with id %s not found", id))));
    }

    public Set<ItemDto> getItems(long userId) {
        return itemRepository.getItems()
                .orElseThrow(() -> new EntityNotFoundException("items not found"))
                .stream()
                .filter(item -> item.getUser().getId() == userId)
                .map(ItemMapper.ITEM_MAPPER::toDto)
                .collect(Collectors.toSet());
    }

    public ItemDto deleteItem(long id) {
        return ItemMapper.ITEM_MAPPER.toDto(itemRepository.deleteItem(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("item with id %s not found", id))));
    }

    public Set<ItemDto> searchItem(String text) {
        if (text.isBlank()) {
            return Collections.emptySet();
        }
        return Optional.of(itemRepository.getItems()
                        .orElseThrow(() -> new EntityNotFoundException("items not found while searching"))
                        .stream()
                        .filter(item -> item.getAvailable() &&
                                (item.getName().toLowerCase().contains(text.toLowerCase())
                                        || item.getDescription().toLowerCase().contains(text.toLowerCase())))
                        .map(ItemMapper.ITEM_MAPPER::toDto)
                        .collect(Collectors.toSet()))
                .orElseThrow(() -> new EntityNotFoundException("there is no item which satisfies your search"));
    }

    private boolean userDoesExist(long userId) {
        return userRepository.getUsers()
                .orElseThrow(() -> new EntityNotFoundException("users are empty"))
                .stream()
                .map(User::getId)
                .anyMatch(id -> id == userId);
    }

    private boolean userIsTheSame(long itemId, long userId) {
        Item item = itemRepository.getItem(itemId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("cannot find Item with %s id", itemId)));
        User user = userRepository.getUser(userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("cannot find User with %s id", userId)));
        return item.getUser().equals(user);
    }
}
