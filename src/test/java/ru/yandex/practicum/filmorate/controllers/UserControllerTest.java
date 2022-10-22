package ru.yandex.practicum.filmorate.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void getUsers() throws Exception {
        User user1 = userService.createUser(
                new User("test1@yandex.ru", "login1", "name1", LocalDate.of(2000,1,1)));
        User user2 = userService.createUser(
                new User("test2@yandex.ru", "login2", "name2", LocalDate.of(1990,1,1)));

        String list = objectMapper.writeValueAsString(List.of(user1, user2));

        mockMvc.perform(
                        get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().json(list));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void createUser() throws Exception {
        User user = new User("test@yandex.ru", "login", "name", LocalDate.of(2000,1,1));
        mockMvc.perform(
                        post("/users")
                                .content(objectMapper.writeValueAsString(user))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.email").value("test@yandex.ru"))
                .andExpect(jsonPath("$.name").value("name"))
                .andExpect(jsonPath("$.login").value("login"))
                .andExpect(jsonPath("$.birthday").value(LocalDate.of(2000,1,1).toString()));

        User result = userService.getUserById(1);

        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(user.getName(), result.getName());
        assertEquals(user.getLogin(), result.getLogin());
        assertEquals(user.getBirthday(), result.getBirthday());

    }

    @Test
    void createUserFailLogin() throws Exception {

        User user = new User("test@yandex.ru", "", "name", LocalDate.of(2000,1,1));
        mockMvc.perform(
                        post("/users")
                                .content(objectMapper.writeValueAsString(user))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

    }

    @Test
    void createUserFailEmail() throws Exception {

        User user = new User("", "login", "name", LocalDate.of(2000,1,1));
        mockMvc.perform(
                        post("/users")
                                .content(objectMapper.writeValueAsString(user))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

        user.setEmail("yandex.ru");
        mockMvc.perform(
                        post("/users")
                                .content(objectMapper.writeValueAsString(user))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

    }

    @Test
    void createUserFailBirthDay() throws Exception {

        User user = new User("test@yandex.ru", "login", "name", LocalDate.of(2023,1,1));
        mockMvc.perform(
                        post("/users")
                                .content(objectMapper.writeValueAsString(user))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

    }

    @Test
    void updateUser() throws Exception {

        User user = userService.createUser(
                new User("test@yandex.ru", "login", "name", LocalDate.of(2000,1,1)));

        user.setName("updated name");
        user.setLogin("updated login");
        user.setEmail("new@yandex.ru");
        user.setBirthday(LocalDate.of(1990,1,1));

        mockMvc.perform(
                        put("/users")
                                .content(objectMapper.writeValueAsString(user))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.email").value("new@yandex.ru"))
                .andExpect(jsonPath("$.name").value("updated name"))
                .andExpect(jsonPath("$.login").value("updated login"))
                .andExpect(jsonPath("$.birthday").value(LocalDate.of(1990,1,1).toString()));

        User result = userService.getUserById(user.getId());

        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(user.getName(), result.getName());
        assertEquals(user.getLogin(), result.getLogin());
        assertEquals(user.getBirthday(), result.getBirthday());

    }

    @Test
    void updateUserFailNotFound() throws Exception {

        User user = new User("test@yandex.ru", "login", "name", LocalDate.of(2000,1,1));
        mockMvc.perform(
                        put("/users")
                                .content(objectMapper.writeValueAsString(user))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException));

    }

    @Test
    void getUser() throws Exception {
        User user = userService.createUser(
                new User("test1@yandex.ru", "login1", "name1", LocalDate.of(2000,1,1)));

        String json = objectMapper.writeValueAsString(user);

        mockMvc.perform(
                        get("/users/" + user.getId()))
                .andExpect(status().isOk())
                .andExpect(content().json(json));
    }

    @Test
    void getUserFailNotFound() throws Exception {

        mockMvc.perform(
                        get("/users/-1"))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException));
    }

    @Test
    void addToFriends() throws Exception {
        User user = userService.createUser(
                new User("test1@yandex.ru", "login1", "name1", LocalDate.of(2000,1,1)));

        User otherUser = userService.createUser(
                new User("test2@yandex.ru", "login2", "name2", LocalDate.of(2001,2,2)));

        mockMvc.perform(
                        put("/users/" + user.getId() + "/friends/" + otherUser.getId()))
                .andExpect(status().isOk());

        List<User> result = userService.getUserFriends(user.getId());

        assertTrue(result.contains(otherUser));
        assertEquals(1, result.size());

        User resultUser = userService.getUserById(user.getId());

        assertEquals(User.ConnectionType.Unconfirmed, resultUser.getFriends().stream()
                .filter(friend -> friend.getFriendId() == otherUser.getId())
                .findFirst()
                .orElseThrow().getConnectionType());

    }

    @Test
    void addToFriendsFailNotFound() throws Exception {
        User user = userService.createUser(
                new User("test1@yandex.ru", "login1", "name1", LocalDate.of(2000,1,1)));

        mockMvc.perform(
                        put("/users/" + user.getId() + "/friends/-1"))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException));
    }

    @Test
    void removeFromFriends() throws Exception {
        User user = userService.createUser(
                new User("test1@yandex.ru", "login1", "name1", LocalDate.of(2000,1,1)));

        User otherUser = userService.createUser(
                new User("test2@yandex.ru", "login2", "name2", LocalDate.of(2001,2,2)));

        userService.addUserToFriends(user.getId(), otherUser.getId());

        mockMvc.perform(
                        delete("/users/" + user.getId() + "/friends/" + otherUser.getId()))
                .andExpect(status().isOk());

        List<User> result = userService.getUserFriends(user.getId());

        assertEquals(0, result.size());
    }

    @Test
    void removeFromFriendsFailNotFound() throws Exception {
        User user = userService.createUser(
                new User("test1@yandex.ru", "login1", "name1", LocalDate.of(2000,1,1)));

        mockMvc.perform(
                        delete("/users/" + user.getId() + "/friends/-1"))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException));
    }

    @Test
    void getFriends() throws Exception {
        User user1 = userService.createUser(
                new User("test1@yandex.ru", "login1", "name1", LocalDate.of(2000,1,1)));

        User user2 = userService.createUser(
                new User("test2@yandex.ru", "login2", "name2", LocalDate.of(2001,2,2)));

        User user3 = userService.createUser(
                new User("test3@yandex.ru", "login3", "name3", LocalDate.of(2002,3,3)));

        userService.addUserToFriends(user1.getId(), user2.getId());
        userService.addUserToFriends(user1.getId(), user3.getId());

        mockMvc.perform(
                        get("/users/ " + user1.getId() + "/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id").value(user2.getId()))
                .andExpect(jsonPath("$.[1].id").value(user3.getId()))
                .andExpect(jsonPath("$", hasSize(2)));


        mockMvc.perform(
                        get("/users/ " + user2.getId() + "/friends"))
                .andExpect(jsonPath("$", hasSize(0)));

    }

    @Test
    void getCommonFriends() throws Exception {
        User user1 = userService.createUser(
                new User("test1@yandex.ru", "login1", "name1", LocalDate.of(2000,1,1)));

        User user2 = userService.createUser(
                new User("test2@yandex.ru", "login2", "name2", LocalDate.of(2001,2,2)));

        User user3 = userService.createUser(
                new User("test3@yandex.ru", "login3", "name3", LocalDate.of(2002,3,3)));

        userService.addUserToFriends(user1.getId(), user3.getId());
        userService.addUserToFriends(user2.getId(), user3.getId());

        mockMvc.perform(
                        get("/users/ " + user1.getId() + "/friends/common/" + user2.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id").value(user3.getId()))
                .andExpect(jsonPath("$", hasSize(1)));

        List<User> result = userService.getCommonFriends(user1.getId(), user2.getId());

        assertEquals(user3.getId(), result.get(0).getId());
        assertEquals(1, result.size());

    }

}