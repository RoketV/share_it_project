package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.model.Item;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemInMemoryRepositoryTest {

    private final InMemoryItemRepository itemRepository;

    @BeforeEach
    public void afterEach() {
        itemRepository.getItems().ifPresent(items -> items.forEach(item -> itemRepository.deleteItem(item.getId())));
        itemRepository.setItemId(0);
    }

    @Test
    @DisplayName("tests add and get methods")
    public void addGetItemTest() {
        Item item = new Item();
        item.setName("name");
        item.setDescription("description");
        item.setAvailable(true);
        itemRepository.addItem(item);
        Assertions.assertEquals(item, itemRepository.getItem(1).get());
    }

    @Test
    @DisplayName("tests update method")
    public void updateItemTest() {
        Item item = new Item();
        item.setName("name");
        item.setDescription("description");
        item.setAvailable(true);
        Item secondItem = new Item();
        item.setName("SecondName");
        item.setDescription("secondDescription");
        item.setAvailable(true);
        itemRepository.addItem(item);
        itemRepository.addItem(secondItem);
        Assertions.assertEquals(item, itemRepository.getItem(1).get());
        Item updateItem = new Item();
        updateItem.setId(1);
        updateItem.setName("newName");
        itemRepository.updateItem(updateItem);
        Assertions.assertAll(
                () -> Assertions.assertEquals("newName", itemRepository.getItem(1).get().getName()),
                () -> Assertions.assertEquals("secondDescription", itemRepository.getItem(1).get().getDescription())
        );
    }

    @Test
    @DisplayName("tests correct delete of item")
    public void deleteItem() {
        Item item = new Item();
        item.setName("name");
        item.setDescription("description");
        item.setAvailable(true);
        itemRepository.addItem(item);
        Assertions.assertTrue(itemRepository.getItems().get().contains(item));
        itemRepository.deleteItem(1);
        Assertions.assertFalse(itemRepository.getItems().get().contains(item));
    }
}
