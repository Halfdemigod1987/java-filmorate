package ru.yandex.practicum.filmorate.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void getUsers() throws Exception {
        User user1 = new User("test1@yandex.ru", "login1", "name1", LocalDate.of(2000,1,1));
        user1.setId();
        User user2 = new User("test2@yandex.ru", "login2", "name2", LocalDate.of(1990,1,1));
        user2.setId();
        String list = objectMapper.writeValueAsString(List.of(user1, user2));

        mockMvc.perform(
                        post("/users")
                                .content(objectMapper.writeValueAsString(user1))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(
                        post("/users")
                                .content(objectMapper.writeValueAsString(user2))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(
                        get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().json(list));
    }

    @Test
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

        User user = new User("test@yandex.ru", "login", "name", LocalDate.of(2000,1,1));
        user.setId();
        mockMvc.perform(
                        post("/users")
                                .content(objectMapper.writeValueAsString(user))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

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
        User user = new User("test1@yandex.ru", "login1", "name1", LocalDate.of(2000,1,1));
        user.setId();
        String json = objectMapper.writeValueAsString(user);

        mockMvc.perform(
                        post("/users")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(
                        get("/users/" + user.getId()))
                .andExpect(status().isOk())
                .andExpect(content().json(json));
    }

    @Test
    void getUserFailNotFound() throws Exception {
        User user = new User("test1@yandex.ru", "login1", "name1", LocalDate.of(2000,1,1));
        user.setId();

        mockMvc.perform(
                        post("/users")
                                .content(objectMapper.writeValueAsString(user))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(
                        get("/users/-1"))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException));
    }

    @Test
    void addToFriends() throws Exception {
        User user = new User("test1@yandex.ru", "login1", "name1", LocalDate.of(2000,1,1));
        user.setId();

        User otherUser = new User("test2@yandex.ru", "login2", "name2", LocalDate.of(2001,2,2));
        otherUser.setId();

        mockMvc.perform(
                        post("/users")
                                .content(objectMapper.writeValueAsString(user))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(
                        post("/users")
                                .content(objectMapper.writeValueAsString(otherUser))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(
                        put("/users/" + user.getId() + "/friends/" + otherUser.getId()))
                .andExpect(status().isOk());

    }

    @Test
    void addToFriendsFailNotFound() throws Exception {
        User user = new User("test1@yandex.ru", "login1", "name1", LocalDate.of(2000,1,1));
        user.setId();

        User otherUser = new User("test2@yandex.ru", "login2", "name2", LocalDate.of(2001,2,2));
        otherUser.setId();

        mockMvc.perform(
                        post("/users")
                                .content(objectMapper.writeValueAsString(user))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(
                        post("/users")
                                .content(objectMapper.writeValueAsString(otherUser))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(
                        put("/users/" + user.getId() + "/friends/-1"))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException));
    }

    @Test
    void removeFromFriends() throws Exception {
        User user = new User("test1@yandex.ru", "login1", "name1", LocalDate.of(2000,1,1));
        user.setId();

        User otherUser = new User("test2@yandex.ru", "login2", "name2", LocalDate.of(2001,2,2));
        otherUser.setId();

        mockMvc.perform(
                        post("/users")
                                .content(objectMapper.writeValueAsString(user))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(
                        post("/users")
                                .content(objectMapper.writeValueAsString(otherUser))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(
                        put("/users/" + user.getId() + "/friends/" + otherUser.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(
                        delete("/users/" + user.getId() + "/friends/" + otherUser.getId()))
                .andExpect(status().isOk());
    }

    @Test
    void removeFromFriendsFailNotFound() throws Exception {
        User user = new User("test1@yandex.ru", "login1", "name1", LocalDate.of(2000,1,1));
        user.setId();

        User otherUser = new User("test2@yandex.ru", "login2", "name2", LocalDate.of(2001,2,2));
        otherUser.setId();

        mockMvc.perform(
                        post("/users")
                                .content(objectMapper.writeValueAsString(user))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(
                        post("/users")
                                .content(objectMapper.writeValueAsString(otherUser))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(
                        put("/users/" + user.getId() + "/friends/" + otherUser.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(
                        delete("/users/" + user.getId() + "/friends/-1"))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException));
    }

    @Test
    void getFriends() throws Exception {
        User user1 = new User("test1@yandex.ru", "login1", "name1", LocalDate.of(2000,1,1));
        user1.setId();

        User user2 = new User("test2@yandex.ru", "login2", "name2", LocalDate.of(2001,2,2));
        user2.setId();

        User user3 = new User("test3@yandex.ru", "login3", "name3", LocalDate.of(2002,3,3));
        user3.setId();

        mockMvc.perform(
                        post("/users")
                                .content(objectMapper.writeValueAsString(user1))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(
                        post("/users")
                                .content(objectMapper.writeValueAsString(user2))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(
                        post("/users")
                                .content(objectMapper.writeValueAsString(user3))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(
                        put("/users/" + user1.getId() + "/friends/" + user2.getId()))
                .andExpect(status().isOk());
        mockMvc.perform(
                        put("/users/" + user1.getId() + "/friends/" + user3.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(
                        get("/users/ " + user1.getId() + "/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id").value(user2.getId()))
                .andExpect(jsonPath("$.[1].id").value(user3.getId()))
                .andExpect(jsonPath("$", hasSize(2)));


        mockMvc.perform(
                        get("/users/ " + user2.getId() + "/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id").value(user1.getId()))
                .andExpect(jsonPath("$", hasSize(1)));

    }

    @Test
    void getCommonFriends() throws Exception {
        User user1 = new User("test1@yandex.ru", "login1", "name1", LocalDate.of(2000,1,1));
        user1.setId();

        User user2 = new User("test2@yandex.ru", "login2", "name2", LocalDate.of(2001,2,2));
        user2.setId();

        User user3 = new User("test3@yandex.ru", "login3", "name3", LocalDate.of(2002,3,3));
        user3.setId();

        mockMvc.perform(
                        post("/users")
                                .content(objectMapper.writeValueAsString(user1))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(
                        post("/users")
                                .content(objectMapper.writeValueAsString(user2))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(
                        post("/users")
                                .content(objectMapper.writeValueAsString(user3))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(
                        put("/users/" + user1.getId() + "/friends/" + user2.getId()))
                .andExpect(status().isOk());
        mockMvc.perform(
                        put("/users/" + user1.getId() + "/friends/" + user3.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(
                        get("/users/ " + user2.getId() + "/friends/common/" + user3.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id").value(user1.getId()))
                .andExpect(jsonPath("$", hasSize(1)));
    }

}