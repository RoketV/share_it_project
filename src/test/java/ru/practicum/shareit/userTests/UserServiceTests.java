package ru.practicum.shareit.userTests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.practicum.shareit.exceptions.EntityNotFoundException;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureTestDatabase
@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceTests {

    private final UserDto userDto = new UserDto(1L,"name", "email@email.com");
    @MockBean
    private UserRepository userRepository;

    @InjectMocks
    private final UserService userService;

    @Test
    void addUserTest() {
        User user = UserMapper.USER_MAPPER.toUser(userDto);
        when(userRepository.save(any())).thenReturn(user);
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
        User user = UserMapper.USER_MAPPER.toUser(userDto);
        user.setName("new Name");
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        UserDto updatedUserDto = userService.updateUser(userDto, user.getId());

        Assertions.assertAll(
                () -> Assertions.assertEquals(user.getId(), updatedUserDto.getId()),
                () -> Assertions.assertEquals(user.getName(), updatedUserDto.getName()),
                () -> Assertions.assertEquals(user.getEmail(), updatedUserDto.getEmail())
        );
    }

    @Test
    public void testGetUser() {
        User user = UserMapper.USER_MAPPER.toUser(userDto);
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        UserDto retrievedUserDto = userService.getUser(user.getId());

        Assertions.assertAll(
                () -> Assertions.assertEquals(user.getId(), retrievedUserDto.getId()),
                () -> Assertions.assertEquals(userDto.getName(), retrievedUserDto.getName()),
                () -> Assertions.assertEquals(userDto.getEmail(), retrievedUserDto.getEmail())
        );
    }

    @Test
    public void testGetUsers() {
        UserDto userDto2 = new UserDto(2L, "Jane Doe", "jane.doe@example.com");
        User user1 = UserMapper.USER_MAPPER.toUser(userDto);
        User user2 = UserMapper.USER_MAPPER.toUser(userDto2);
        List<User> users = Arrays.asList(user1, user2);

        when(userRepository.findAll()).thenReturn(users);

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
        User user = UserMapper.USER_MAPPER.toUser(userDto);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        UserDto deletedUserDto = userService.deleteUser(user.getId());

        Assertions.assertAll(
                () -> Assertions.assertEquals(user.getId(), deletedUserDto.getId()),
                () -> Assertions.assertEquals(userDto.getName(), deletedUserDto.getName()),
                () -> Assertions.assertEquals(userDto.getEmail(), deletedUserDto.getEmail()),
                () -> Assertions.assertThrows(EntityNotFoundException.class, () -> userService.getUser(user.getId()+1)));
    }
}

