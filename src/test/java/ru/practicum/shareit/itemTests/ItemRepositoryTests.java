package ru.practicum.shareit.itemTests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ItemRepositoryTests {

    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRequestRepository requestRepository;

    private Item item1;
    private Item item2;
    private User user;
    private ItemRequest request;


    @BeforeEach
    public void setUp() {
        Long ownerId = 1L;
        user = new User();
        user.setId(ownerId);
        user.setName("User 1");
        user.setEmail("user1@example.com");

        userRepository.save(user);

        request = new ItemRequest();
        request.setCreated(LocalDateTime.now());
        request.setUser(user);
        request.setDescription("Request Description");
        request.setId(1L);

        requestRepository.save(request);

        item1 = new Item();
        item1.setName("Item 1");
        item1.setDescription("Description 1");
        item1.setAvailable(true);
        item1.setUser(user);
        item1.setRequest(request);

        item2 = new Item();
        item2.setName("Item 2");
        item2.setDescription("Description 2");
        item2.setAvailable(false);
        item2.setUser(user);
        item2.setRequest(request);

        itemRepository.saveAll(List.of(item1, item2));
    }

    @Test
    void testFindAllByUser_IdOrderByIdDesc() {
        Page<Item> itemsPage = itemRepository.findAllByUser_IdOrderByIdDesc(user.getId(), PageRequest.of(0, 10));

        Assertions.assertAll(
                () -> assertEquals(2, itemsPage.getContent().size()),
                () -> assertTrue(itemsPage.getContent().contains(item1)),
                () -> assertTrue(itemsPage.getContent().contains(item2))
        );
    }

    @Test
    public void testFindItemsByRequestId() {
        List<Item> items = itemRepository.findItemsByRequestId(request.getId());

        assertEquals(2, items.size());
        assertTrue(items.contains(item1));
        assertTrue(items.contains(item2));
    }
}