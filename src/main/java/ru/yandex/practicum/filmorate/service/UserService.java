package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dao.UserStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    public User getUserById(long userId) {
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

    public User addUserToFriends(long userId, long otherUserId) {
        User user = getUserById(userId);
        User otherUser = getUserById(otherUserId);
        if (user.getFriends().stream().noneMatch(friend -> friend.getFriendId() == otherUserId)) {
            User.Friend friend = new User.Friend(userId, otherUserId, User.ConnectionType.Unconfirmed);
            userStorage.addFriend(user, friend);
        }
        return user;
    }

    public User confirmUserAsFriend(long userId, long otherUserId) {
        User user = getUserById(userId);
        User otherUser = getUserById(otherUserId);
        if (user.getFriends().stream().anyMatch(friend -> friend.getFriendId() == otherUserId)) {
            User.Friend friend = new User.Friend(userId, otherUserId, User.ConnectionType.Confirmed);
            userStorage.updateFriend(user, friend);
        }
        return user;
    }

    public User deleteUserFromFriends(long userId, long otherUserId) {
        User user = getUserById(userId);
        getUserById(otherUserId);
        if (user.getFriends().stream().anyMatch(friend -> friend.getFriendId() == otherUserId)) {
            userStorage.deleteFriend(user, otherUserId);
        }
        return user;
    }

    public List<User> getUserFriends(long userId) {
        User user = getUserById(userId);
        return userStorage.findById(user.getFriends().stream()
                .map(User.Friend::getFriendId)
                .collect(Collectors.toSet()));
    }

    public List<User> getCommonFriends(long userId, long otherId) {
        User user      = getUserById(userId);
        User otherUser = getUserById(otherId);

        Set<Long> intersection = user.getFriends().stream()
                .map(User.Friend::getFriendId)
                .collect(Collectors.toSet());
        intersection.retainAll(otherUser.getFriends().stream()
                .map(User.Friend::getFriendId)
                .collect(Collectors.toSet()));

        return userStorage.findById(intersection);
    }
}
