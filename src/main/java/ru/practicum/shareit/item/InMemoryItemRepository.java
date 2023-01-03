package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exceptions.EntityNotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Repository
@Slf4j
@RequiredArgsConstructor
public class InMemoryItemRepository implements ItemRepository {

    private final Map<Long, Item> items;
    private long itemId;

    @Override
    public Optional<Item> addItem(Item item) {
        item.setId(itemId + 1);
        items.put(item.getId(), item);
        log.info("item added");
        itemId = itemId + 1;
        return Optional.of(item);
    }

    @Override
    public Optional<Item> updateItem(Item item) {
        if (!itemDoesExist(item)) {
            throw new EntityNotFoundException(String.format("item to update with %s id not found", item.getId()));
        }
        Item oldItem = items.get(item.getId());
        if (item.getAvailable() != null) {
            oldItem.setAvailable(item.getAvailable());
        }
        if (item.getName() != null) {
            oldItem.setName(item.getName());
        }
        if (item.getDescription() != null) {
            oldItem.setDescription(item.getDescription());
        }
        items.put(oldItem.getId(), oldItem);
        log.info("item with id {} updated", item.getId());
        return Optional.of(items.get(itemId));
    }

    @Override
    public Optional<Item> getItem(long id) {
        return Optional.of(items.get(id));
    }

    @Override
    public Optional<Set<Item>> getItems() {
        return Optional.of(new HashSet<>(items.values()));
    }

    @Override
    public Optional<Item> deleteItem(long id) {
        Item item = items.get(id);
        items.remove(id);
        log.info("user with id {} deleted", id);
        return Optional.of(item);
    }

    private boolean itemDoesExist(Item item) {
        return items.containsKey(item.getId());
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }
}
