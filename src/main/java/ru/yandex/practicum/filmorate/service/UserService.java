package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author kazakov
 * @version 16.09.2022
 */
@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> getUsers() {
        return userStorage.findAll();
    }

    public User getUserById(int userId) {
        Optional<User> optionalUser = userStorage.findById(userId);
        if (optionalUser.isEmpty()) {
            log.warn("Не найден пользователь с id: {}", userId);
            throw new NotFoundException(String.format("Пользователь с id = %d не найден",userId));
        }
        return optionalUser.get();
    }

    public User createUser(User user) {
        return userStorage.save(user);
    }

    public User updateUser(User user) {
        Optional<User> optionalUser = userStorage.findById(user.getId());
        if (optionalUser.isEmpty()) {
            log.error("Не найден пользователь: {}", user);
            throw new NotFoundException(String.format("Пользователь с id = %d не найден",user.getId()));
        }

        return userStorage.save(user);
    }

    public User addUserToFriends(int userId, int friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);
        user.addToFriends(friendId);
        friend.addToFriends(userId);
        updateUser(user);
        updateUser(friend);
        return user;
    }

    public User deleteUserFromFriends(int userId, int friendId) {
        User user   = getUserById(userId);
        User friend = getUserById(friendId);
        user.deleteFromFriends(friendId);
        friend.deleteFromFriends(userId);
        updateUser(user);
        updateUser(friend);
        return user;
    }

    public List<User> getUserFriends(int userId) {
        User user = getUserById(userId);
        return userStorage.findById(user.getFriends());
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        User user      = getUserById(userId);
        User otherUser = getUserById(otherId);

        Set<Long> intersection = new HashSet<>(user.getFriends());
        intersection.retainAll(otherUser.getFriends());

        return userStorage.findById(intersection);
    }
}
