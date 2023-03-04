package ru.practicum.shareit.itemRequestTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ItemRequestRepositoryTests {

    @Autowired
    private ItemRequestRepository itemRequestRepository;
    @Autowired
    private UserRepository userRepository;
    private User user1;
    private User user2;
    private ItemRequest request1;
    private ItemRequest request2;
    private ItemRequest request3;


    @BeforeEach
    public void setUp() {
        user1 = new User(1L, "name User1", "email@email.com");
        user2 = new User(2L, "name User2", "email1@email.com");
        userRepository.saveAll(List.of(user1, user2));

        request1 = new ItemRequest(1L, "description Request2", LocalDateTime.now(), user1);
        request2 = new ItemRequest(2L, "description Request2", LocalDateTime.now(), user1);
        request3 = new ItemRequest(3L, "description Request2", LocalDateTime.now(), user2);
        itemRequestRepository.saveAll(List.of(request1, request2, request3));
    }

    @Test
    public void testFindAllByUser_Id() {
        Long userId = 1L;
        List<ItemRequest> itemRequests = itemRequestRepository.findAllByUser_Id(userId);
        assertAll(
                () -> assertEquals(2, itemRequests.size()),
                () -> assertTrue(itemRequests.contains(request1)),
                () -> assertTrue(itemRequests.contains(request2)),
                () -> assertFalse(itemRequests.contains(request3))
        );
    }

    @Test
    public void testFindAllExceptUser() {
        Long userId = 1L;
        Page<ItemRequest> itemRequests = itemRequestRepository.findAllExceptUser(userId, PageRequest.of(0,20));
        assertAll(
                () -> assertEquals(1, itemRequests.getContent().size()),
                () -> assertFalse(itemRequests.getContent().contains(request1)),
                () -> assertFalse(itemRequests.getContent().contains(request2)),
                () -> assertTrue(itemRequests.getContent().contains(request3))
        );
    }
}
