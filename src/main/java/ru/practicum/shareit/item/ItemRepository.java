package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.Optional;
import java.util.Set;

public interface ItemRepository {

    Optional<Item> addItem(Item item);

    Optional<Item> updateItem(Item item);

    Optional<Item> getItem(long id);

    Optional<Set<Item>> getItems();

    Optional<Item> deleteItem(long id);
}
