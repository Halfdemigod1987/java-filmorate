package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dao.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author kazakov
 * @version 16.09.2022
 */
@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User save(User user) {
        if (user.getId() == 0) {
            user.setId();
            log.debug("Добавлен пользователь: {}", user);
        } else {
            log.debug("Обновлен пользователь: {}", user);
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public Optional<User> findById(long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> findById(Set<Long> ids) {
        return users.entrySet().stream()
                .filter(x -> ids.contains(x.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    @Override
    public User addFriend(User user, long friendId, User.Connection connection) {
        user.addToFriends(friendId, connection);
        return user;
    }

    @Override
    public User updateFriend(User user, long friendId, User.Connection connection) {
        user.addToFriends(friendId, connection);
        return user;
    }

    @Override
    public User deleteFriend(User user, long friendId) {
        user.deleteFromFriends(friendId);
        return user;
    }
}
