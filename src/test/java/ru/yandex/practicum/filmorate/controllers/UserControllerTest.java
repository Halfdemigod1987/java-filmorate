package ru.yandex.practicum.filmorate.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

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
        User user2 = new User("test2@yandex.ru", "login2", "name2", LocalDate.of(1990,1,1));
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
                .andExpect(jsonPath("$.id").value(user.getId()))
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
    void createUserFailAlreadyExist() throws Exception {
        User user = new User("test@yandex.ru", "login", "name", LocalDate.of(2000,1,1));
        mockMvc.perform(
                        post("/users")
                                .content(objectMapper.writeValueAsString(user))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(
                        post("/users")
                                .content(objectMapper.writeValueAsString(user))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserController.UserAlreadyExistException));
    }

    @Test
    void updateUser() throws Exception {

        User user = new User("test@yandex.ru", "login", "name", LocalDate.of(2000,1,1));
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
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserController.UserNotFoundException));

    }

}