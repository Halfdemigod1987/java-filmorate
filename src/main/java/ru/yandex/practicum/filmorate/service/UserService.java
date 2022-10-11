package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author kazakov
 * @version 16.09.2022
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public List<User> getUsers() {
        return userStorage.findAll();
    }

    public User getUserById(int userId) {
        return userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id = %d не найден",userId)));
    }

    public User createUser(User user) {
        return userStorage.save(user);
    }

    public User updateUser(User user) {
        userStorage.findById(user.getId())
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id = %d не найден",user.getId())));

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
        return userStorage.findById(user.getFriends().keySet());
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        User user      = getUserById(userId);
        User otherUser = getUserById(otherId);

        Set<Long> intersection = new HashSet<>(user.getFriends().keySet());
        intersection.retainAll(otherUser.getFriends().keySet());

        return userStorage.findById(intersection);
    }
}
