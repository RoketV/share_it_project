package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exceptions.EntityNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.model.User;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
public class InMemoryUserRepository implements UserRepository {

    private final Map<Long, User> users;
    private final Set<String> uniqueEmails;
    private int userId;

    @Override
    public Optional<User> addUser(User user) {
        user.setId(userId + 1);
        if (isNotUniqueEmail(user)) {
            throw new ValidationException(String.format("user with %s email already exists", user.getEmail()));
        }
        users.put(user.getId(), user);
        uniqueEmails.add(user.getEmail());
        log.info("user was created");
        userId = userId + 1;
        return Optional.of(user);
    }

    @Override
    public Optional<User> updateUser(User user) {
        if (!doesExist(user)) {
            throw new EntityNotFoundException(String.format("user with %d id not found", user.getId()));
        }
        User oldUser = users.get(user.getId());
        if (isNotUniqueEmail(user)) {
            throw new ValidationException(String.format("user with %s email already exists", user.getEmail()));
        }
        if (user.getEmail() != null) {
            uniqueEmails.remove(oldUser.getEmail());
            oldUser.setEmail(user.getEmail());
            uniqueEmails.add(user.getEmail());
        }
        if (user.getName() != null) {
            oldUser.setName(user.getName());
        }
        users.put(user.getId(), oldUser);
        log.info("user with id {} was updated", user.getId());
        return Optional.of(oldUser);
    }

    @Override
    public Optional<User> getUser(long id) {
        return Optional.of(users.get(id));
    }

    @Override
    public Set<User> getUsers() {
        return new HashSet<>(users.values());
    }

    @Override
    public Optional<User> deleteUser(long id) {
        if (!doesExist(users.get(id))) {
            throw new EntityNotFoundException(String.format("user with %d id not found", id));
        }
        User user = users.get(id);
        uniqueEmails.remove(user.getEmail());
        users.remove(id);
        log.info("user with id {} was deleted", user.getId());
        return Optional.of(user);
    }

    private boolean isNotUniqueEmail(User user) {
        return uniqueEmails.contains(user.getEmail());
    }

    private boolean doesExist(User user) {
        return users.containsKey(user.getId());
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
