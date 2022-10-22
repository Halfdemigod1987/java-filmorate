package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
class UserServiceTest {
    @Autowired
    private UserService userService;

    @Test
    void confirmUserAsFriend() {
        User user = userService.createUser(
                new User("test1@yandex.ru", "login1", "name1", LocalDate.of(2000,1,1)));

        User otherUser = userService.createUser(
                new User("test2@yandex.ru", "login2", "name2", LocalDate.of(2001,2,2)));

        userService.addUserToFriends(user.getId(), otherUser.getId());

        userService.confirmUserAsFriend(user.getId(), otherUser.getId());

        User result = userService.getUserById(user.getId());

        assertEquals(User.ConnectionType.Confirmed, result.getFriends().stream()
                .filter(friend -> friend.getFriendId() == otherUser.getId())
                .findFirst()
                .orElseThrow().getConnectionType());
    }

    @Test
    void confirmUserAsFriendFailed() {
        User user = userService.createUser(
                new User("test1@yandex.ru", "login1", "name1", LocalDate.of(2000,1,1)));

        User otherUser = userService.createUser(
                new User("test2@yandex.ru", "login2", "name2", LocalDate.of(2001,2,2)));

        userService.addUserToFriends(user.getId(), otherUser.getId());

        assertThrows(NotFoundException.class, () -> userService.confirmUserAsFriend(user.getId(), -1));

    }
}