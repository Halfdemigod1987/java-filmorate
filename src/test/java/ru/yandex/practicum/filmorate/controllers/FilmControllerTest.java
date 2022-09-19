package ru.yandex.practicum.filmorate.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
class FilmControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void getFilms() throws Exception {
        Film film1 = new Film("Name1", "Description1", LocalDate.now(), 100);
        film1.setId();
        Film film2 = new Film("Name2", "Description2", LocalDate.now().minusDays(50), 200);
        film2.setId();
        String list = objectMapper.writeValueAsString(List.of(film1, film2));

        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film1))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film2))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(
                        get("/films"))
                .andExpect(status().isOk())
                .andExpect(content().json(list));
    }

    @Test
    void createFilm() throws Exception {

        LocalDate releaseDate = LocalDate.now();
        Film film = new Film("Name", "Description", releaseDate, 100);
        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Name"))
                .andExpect(jsonPath("$.description").value("Description"))
                .andExpect(jsonPath("$.releaseDate").value(releaseDate.toString()))
                .andExpect(jsonPath("$.duration").value(100));

    }

    @Test
    void createFilmFailName() throws Exception {

        Film film = new Film("", "Description", LocalDate.now(), 100);
        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

    }

    @Test
    void createFilmFailDescription() throws Exception {

        String longName = Stream.generate(() -> "x").limit(400).collect(Collectors.joining());
        Film film = new Film("Name", longName, LocalDate.now(), 100);
        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

    }

    @Test
    void createFilmFailReleaseDate() throws Exception {

        Film film = new Film("Name", "Description", LocalDate.of(1890, 3, 25), 100);
        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ValidationException));

    }

    @Test
    void createFilmFailDuration() throws Exception {

        Film film = new Film("Name", "Description", LocalDate.now(), -100);
        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

    }

    @Test
    void updateFilm() throws Exception {

        LocalDate releaseDate = LocalDate.now();
        Film film = new Film("Name", "Description", releaseDate, 100);
        film.setId();
        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        film.setName("Updated name");
        film.setDescription("Updated description");
        film.setReleaseDate(releaseDate.minusDays(100));
        film.setDuration(200);
        mockMvc.perform(
                        put("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(film.getId()))
                .andExpect(jsonPath("$.name").value("Updated name"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.releaseDate").value(releaseDate.minusDays(100).toString()))
                .andExpect(jsonPath("$.duration").value(200));

    }

    @Test
    void updateFilmFailNotFound() throws Exception {

        Film film = new Film("Name", "Description", LocalDate.now(), 100);
        mockMvc.perform(
                        put("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException));

    }

    @Test
    void getFilm() throws Exception {
        Film film = new Film("Name1", "Description1", LocalDate.now(), 100);
        film.setId();
        String json = objectMapper.writeValueAsString(film);

        mockMvc.perform(
                        post("/films")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(
                        get("/films/" + film.getId()))
                .andExpect(status().isOk())
                .andExpect(content().json(json));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void getFilmFailNotFound() throws Exception {
        Film film = new Film("Name1", "Description1", LocalDate.now(), 100);
        String json = objectMapper.writeValueAsString(film);

        mockMvc.perform(
                        post("/films")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(
                        get("/films/2"))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException));
    }

    @Test
    void likeFilm() throws Exception {
        Film film = new Film("Name1", "Description1", LocalDate.now(), 100);
        film.setId();
        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        User user = new User("test1@yandex.ru", "login1", "name1", LocalDate.of(2000,1,1));
        user.setId();
        mockMvc.perform(
                        post("/users")
                                .content(objectMapper.writeValueAsString(user))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(
                        put("/films/" + film.getId() + " /like/" + user.getId()))
                .andExpect(status().isOk());

    }

    @Test
    void likeFilmFailNotFound() throws Exception {
        Film film = new Film("Name1", "Description1", LocalDate.now(), 100);
        film.setId();
        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        User user = new User("test1@yandex.ru", "login1", "name1", LocalDate.of(2000,1,1));
        user.setId();
        mockMvc.perform(
                        post("/users")
                                .content(objectMapper.writeValueAsString(user))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(
                        put("/films/" + film.getId() + "/like/-1"))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException));

    }

    @Test
    void dislikeFilm() throws Exception {
        Film film = new Film("Name1", "Description1", LocalDate.now(), 100);
        film.setId();
        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        User user = new User("test1@yandex.ru", "login1", "name1", LocalDate.of(2000,1,1));
        user.setId();
        mockMvc.perform(
                        post("/users")
                                .content(objectMapper.writeValueAsString(user))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(
                        put("/films/" + film.getId() + "/like/" + user.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(
                        delete("/films/" + film.getId() + "/like/" + user.getId()))
                .andExpect(status().isOk());

    }

    @Test
    void dislikeFilmFailNotFound() throws Exception {
        Film film = new Film("Name1", "Description1", LocalDate.now(), 100);
        film.setId();
        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        User user = new User("test1@yandex.ru", "login1", "name1", LocalDate.of(2000,1,1));
        user.setId();
        mockMvc.perform(
                        post("/users")
                                .content(objectMapper.writeValueAsString(user))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(
                        put("/films/" + film.getId() + "/like/" + user.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(
                        put("/films/" + film.getId() + "/like/-1"))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException));

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void getPopular() throws Exception {
        Film film1 = new Film("Name1", "Description1", LocalDate.now(), 100);
        film1.setId();
        Film film2 = new Film("Name1", "Description1", LocalDate.now(), 100);
        film2.setId();
        Film film3 = new Film("Name1", "Description1", LocalDate.now(), 100);
        film3.setId();

        User user1 = new User("test1@yandex.ru", "login1", "name1", LocalDate.of(2000,1,1));
        user1.setId();
        User user2 = new User("test1@yandex.ru", "login1", "name1", LocalDate.of(2000,1,1));
        user2.setId();
        User user3 = new User("test1@yandex.ru", "login1", "name1", LocalDate.of(2000,1,1));
        user3.setId();

        film1.like(user1.getId());
        film2.like(user1.getId());
        film2.like(user2.getId());
        film2.like(user3.getId());
        film3.like(user1.getId());
        film3.like(user2.getId());

        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film1))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film2))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film3))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
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

        List<Film> films = new ArrayList<>();
        films.add(film2);
        films.add(film3);
        films.add(film1);
        mockMvc.perform(
                        get("/films/popular"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(films)));

        films.remove(film1);
        mockMvc.perform(
                        get("/films/popular?count=2"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(films)));

    }
}