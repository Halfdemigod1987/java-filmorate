package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final Set<User> users = new HashSet<>();

    @GetMapping
    public Set<User> getUsers() {
        return users;
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        if (users.contains(user)) {
            log.warn("Уже существует пользователь: {}", user);
            throw new UserAlreadyExistException();
        }
        users.add(user);
        log.debug("Добавлен пользователь: {}", user);
        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        if (!users.contains(user)) {
            log.warn("Не найден пользователь: {}", user);
            throw new UserNotFoundException();
        }
        users.remove(user);
        users.add(user);
        log.debug("Обновлен пользователь: {}", user);
        return user;
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public static class UserAlreadyExistException extends RuntimeException {}

    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public static class UserNotFoundException extends RuntimeException {}

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public static class UserValidationException extends RuntimeException {}

}
