package ru.practicum.shareit.userTests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exceptions.EntityNotFoundException;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import javax.transaction.Transactional;
import java.util.Iterator;
import java.util.Set;

@SpringBootTest
@AutoConfigureTestDatabase
@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
class UserServiceTests {

    private final UserService userService;
    UserDto userDto = new UserDto("name", "email@email.com");

    @Test
    void addUserTest() {
        UserDto savedUser = userService.addUser(userDto);
        Assertions.assertAll(
                () -> Assertions.assertNotNull(savedUser),
                () -> Assertions.assertEquals(savedUser.getName(), userDto.getName()),
                () -> Assertions.assertEquals(savedUser.getEmail(), userDto.getEmail())
        );
    }

    @Test
    void addUserNullTest() {
        Assertions.assertThrows(NullPointerException.class,
                () -> userService.addUser(null));
    }

    @Test
    public void testUpdateUser() {
        UserDto addedUserDto = userService.addUser(userDto);

        userDto.setName("Jane Smith");
        UserDto updatedUserDto = userService.updateUser(userDto, addedUserDto.getId());

        Assertions.assertAll(
                () -> Assertions.assertEquals(addedUserDto.getId(), updatedUserDto.getId()),
                () -> Assertions.assertEquals(userDto.getName(), updatedUserDto.getName()),
                () -> Assertions.assertEquals(userDto.getEmail(), updatedUserDto.getEmail())
        );
    }

    @Test
    public void testGetUser() {
        UserDto addedUserDto = userService.addUser(userDto);

        UserDto retrievedUserDto = userService.getUser(addedUserDto.getId());

        Assertions.assertAll(
                () -> Assertions.assertEquals(addedUserDto.getId(), retrievedUserDto.getId()),
                () -> Assertions.assertEquals(userDto.getName(), retrievedUserDto.getName()),
                () -> Assertions.assertEquals(userDto.getEmail(), retrievedUserDto.getEmail())
        );
    }

    @Test
    public void testGetUsers() {
        userService.addUser(userDto);

        UserDto userDto2 = new UserDto();
        userDto2.setName("Jane Doe");
        userDto2.setEmail("jane.doe@example.com");
        userService.addUser(userDto2);

        Set<UserDto> retrievedUserDtos = userService.getUsers();

        Assertions.assertEquals(2, retrievedUserDtos.size());
        Iterator<UserDto> iterator = retrievedUserDtos.iterator();
        UserDto retrievedUserDto1 = iterator.next();
        UserDto retrievedUserDto2 = iterator.next();
        Assertions.assertAll(
                () -> Assertions.assertTrue(retrievedUserDto1.getId() < retrievedUserDto2.getId()),
                () -> Assertions.assertEquals(userDto.getName(), retrievedUserDto1.getName()),
                () -> Assertions.assertEquals(userDto.getEmail(), retrievedUserDto1.getEmail()),
                () -> Assertions.assertEquals(userDto2.getName(), retrievedUserDto2.getName()),
                () -> Assertions.assertEquals(userDto2.getEmail(), retrievedUserDto2.getEmail())
        );
    }

    @Test
    void testDeleteUser() {
        UserDto addedUserDto = userService.addUser(userDto);


        UserDto deletedUserDto = userService.deleteUser(addedUserDto.getId());

        Assertions.assertAll(
                () -> Assertions.assertEquals(addedUserDto.getId(), deletedUserDto.getId()),
                () -> Assertions.assertEquals(userDto.getName(), deletedUserDto.getName()),
                () -> Assertions.assertEquals(userDto.getEmail(), deletedUserDto.getEmail()),
                () -> Assertions.assertThrows(EntityNotFoundException.class, () -> userService.getUser(addedUserDto.getId())));
    }
}

