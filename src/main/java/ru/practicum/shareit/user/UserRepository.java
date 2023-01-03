package ru.practicum.shareit.user;

import ru.practicum.shareit.user.model.User;

import java.util.Optional;
import java.util.Set;

public interface UserRepository {

    Optional<User> addUser(User user);

    Optional<User> updateUser(User user);

    Optional<User> getUser(long id);

    Optional<Set<User>> getUsers();

    Optional<User> deleteUser(long id);

}
