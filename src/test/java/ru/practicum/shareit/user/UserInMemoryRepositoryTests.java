package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.user.model.User;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserInMemoryRepositoryTests {

    private final InMemoryUserRepository userRepository;

    @BeforeEach
    public void beforeEach() {
        userRepository.getUsers().ifPresent(users -> users.forEach(user -> userRepository.deleteUser(user.getId())));
        userRepository.setUserId(0);
    }

    @Test
    @DisplayName("checks add and get methods")
    public void addGetUserTest() {
        User user = new User();
        user.setName("name");
        user.setEmail("email@email.ru");
        userRepository.addUser(user);
        Assertions.assertEquals(user, userRepository.getUser(1).get());
    }

    @Test
    @DisplayName("checks update and method")
    public void updateUserTest() {
        User user = new User();
        user.setName("name");
        user.setEmail("email@email.ru");
        userRepository.addUser(user);
        Assertions.assertEquals(user, userRepository.getUser(1).get());
        User newUser = new User(1, "newName", null);
        userRepository.updateUser(newUser);
        Assertions.assertAll(
                () -> Assertions.assertEquals(userRepository.getUser(1).get().getName(), "newName"),
                () -> Assertions.assertEquals(userRepository.getUser(1).get().getEmail(), "email@email.ru")
        );
    }

    @Test
    @DisplayName("checks delete method")
    public void deleteUserTest() {
        User user = new User();
        user.setName("name");
        user.setEmail("email@email.ru");
        User secondUser = new User();
        user.setName("secondName");
        user.setEmail("secondEmail@email.ru");
        userRepository.addUser(user);
        userRepository.addUser(secondUser);
        Assertions.assertAll(
                () -> Assertions.assertEquals(user, userRepository.getUser(1).get()),
                () -> Assertions.assertEquals(secondUser, userRepository.getUser(2).get()),
                () -> Assertions.assertTrue(userRepository.getUsers().get().contains(user)));
        userRepository.deleteUser(1);
        Assertions.assertFalse(userRepository.getUsers().get().contains(user));
    }
}
