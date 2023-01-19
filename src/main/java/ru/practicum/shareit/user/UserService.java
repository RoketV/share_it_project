package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.EntityNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserDto addUser(UserDto dto) {
        return UserMapper.USER_MAPPER.toDto(userRepository.save(UserMapper.USER_MAPPER.toUser(dto)));
    }

    public UserDto updateUser(UserDto dto, Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("user with id %d not found", id)));
        if (dto.getName() != null) {
            user.setName(dto.getName());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        return UserMapper.USER_MAPPER.toDto(userRepository.save(user));
    }

    public UserDto getUser(Long id) {
        return UserMapper.USER_MAPPER.toDto(userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("no user with %d id found", id))));
    }

    public Set<UserDto> getUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper.USER_MAPPER::toDto)
                .sorted(Comparator.comparing(UserDto::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public UserDto deleteUser(Long id) {
        Optional<User> user = userRepository.findById(id);
        user.ifPresent(userRepository::delete);
        return UserMapper.USER_MAPPER.toDto(user
                .orElseThrow(() -> new EntityNotFoundException(String.format("no user with %d id to delete", id))));
    }
}
