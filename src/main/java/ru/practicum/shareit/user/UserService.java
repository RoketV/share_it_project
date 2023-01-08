package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.EntityNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserDto addUser(UserDto dto) {
        return UserMapper.USER_MAPPER.toDto(userRepository.addUser(UserMapper.USER_MAPPER.toUser(dto))
                .orElseThrow(() -> new ValidationException("cannot add this user")));
    }

    public UserDto updateUser(UserDto dto, long id) {
        dto.setId(id);
        return UserMapper.USER_MAPPER.toDto(userRepository.updateUser(UserMapper.USER_MAPPER.toUser(dto))
                .orElseThrow(() -> new EntityNotFoundException("there is no such user to update")));
    }

    public UserDto getUser(long id) {
        return UserMapper.USER_MAPPER.toDto(userRepository.getUser(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("no user with %d id found", id))));
    }

    public Set<UserDto> getUsers() {
        return userRepository.getUsers()
                .stream()
                .map(UserMapper.USER_MAPPER::toDto)
                .collect(Collectors.toSet());
    }

    public UserDto deleteUser(long id) {
        return UserMapper.USER_MAPPER.toDto(userRepository.deleteUser(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("no user with %d id to delete", id))));
    }
}
