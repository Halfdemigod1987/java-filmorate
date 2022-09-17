package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author kazakov
 * @version 16.09.2022
 */
public interface UserStorage {

    User save(User user);

    List<User> findAll();

    Optional<User> findById(long id);

    List<User> findById(Set<Long> ids);

}
