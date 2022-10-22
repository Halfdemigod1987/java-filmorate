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
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
class FilmControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private FilmService filmService;
    @Autowired
    private UserService userService;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void getFilms() throws Exception {

        Film film1 = filmService.createFilm(
                new Film("Name1", "Description1", LocalDate.now(), 100, new MPA(1, "G")));
        Film film2 = filmService.createFilm(
                new Film("Name2", "Description2", LocalDate.now().minusDays(50), 200, new MPA(2, "PG")));
        String list = objectMapper.writeValueAsString(List.of(film1, film2));

        mockMvc.perform(
                        get("/films"))
                .andExpect(status().isOk())
                .andExpect(content().json(list));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void createFilm() throws Exception {

        LocalDate releaseDate = LocalDate.now();
        Film film = new Film("Name", "Description", releaseDate, 100, new MPA(1));
        film.setGenres(Set.of(new Genre(1, "Комедия")));
        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Name"))
                .andExpect(jsonPath("$.description").value("Description"))
                .andExpect(jsonPath("$.releaseDate").value(releaseDate.toString()))
                .andExpect(jsonPath("$.duration").value(100))
                .andExpect(jsonPath("$.genres[0].name").value("Комедия"))
                .andExpect(jsonPath("$.mpa.id").value("1"));

        Film result = filmService.getFilmById(1);

        assertEquals(film.getName(), result.getName());
        assertEquals(film.getDescription(), result.getDescription());
        assertEquals(film.getDescription(), result.getDescription());
        assertEquals(film.getReleaseDate(), result.getReleaseDate());
        assertEquals(film.getGenres(), result.getGenres());
        assertEquals(film.getMpa().getId(), result.getId());

    }

    @Test
    void createFilmFailName() throws Exception {

        Film film = new Film("", "Description", LocalDate.now(), 100, new MPA(1));
        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

    }

    @Test
    void createFilmFailDescription() throws Exception {

        String longName = Stream.generate(() -> "x").limit(400).collect(Collectors.joining());
        Film film = new Film("Name", longName, LocalDate.now(), 100, new MPA(1));
        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

    }

    @Test
    void createFilmFailReleaseDate() throws Exception {

        Film film = new Film("Name", "Description", LocalDate.of(1890, 3, 25), 100, new MPA(1));
        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ValidationException));

    }

    @Test
    void createFilmFailDuration() throws Exception {

        Film film = new Film("Name", "Description", LocalDate.now(), -100, new MPA(1));
        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

    }

    @Test
    void updateFilm() throws Exception {

        LocalDate releaseDate = LocalDate.now();
        Film film = filmService.createFilm(new Film("Name", "Description", releaseDate, 100, new MPA(1)));

        film.setName("Updated name");
        film.setDescription("Updated description");
        film.setReleaseDate(releaseDate.minusDays(100));
        film.setDuration(200);
        film.setMpa(new MPA(2));
        mockMvc.perform(
                        put("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(film.getId()))
                .andExpect(jsonPath("$.name").value("Updated name"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.releaseDate").value(releaseDate.minusDays(100).toString()))
                .andExpect(jsonPath("$.duration").value(200))
                .andExpect(jsonPath("$.mpa.id").value("2"));

        Film result = filmService.getFilmById(film.getId());

        assertEquals(film.getName(), result.getName());
        assertEquals(film.getDescription(), result.getDescription());
        assertEquals(film.getDescription(), result.getDescription());
        assertEquals(film.getReleaseDate(), result.getReleaseDate());
        assertEquals(film.getMpa().getId(), result.getMpa().getId());

    }

    @Test
    void updateFilmFailNotFound() throws Exception {

        Film film = new Film("Name", "Description", LocalDate.now(), 100, new MPA(1));
        mockMvc.perform(
                        put("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException));

    }

    @Test
    void getFilm() throws Exception {
        Film film = filmService.createFilm(
                new Film("Name1", "Description1", LocalDate.now(), 100, new MPA(1, "G")));

        String json = objectMapper.writeValueAsString(film);

        mockMvc.perform(
                        get("/films/" + film.getId()))
                .andExpect(status().isOk())
                .andExpect(content().json(json));
    }

    @Test
    void getFilmFailNotFound() throws Exception {

        mockMvc.perform(
                        get("/films/-1"))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException));
    }

    @Test
    void likeFilm() throws Exception {
        Film film = filmService.createFilm(
                new Film("Name1", "Description1", LocalDate.now(), 100, new MPA(1)));

        User user = userService.createUser(
                new User("test1@yandex.ru", "login1", "name1", LocalDate.of(2000,1,1)));


        mockMvc.perform(
                        put("/films/" + film.getId() + " /like/" + user.getId()))
                .andExpect(status().isOk());

        Film result = filmService.getFilmById(film.getId());

        assertEquals(1, result.getLikes().size());
        assertTrue(result.getLikes().contains(user.getId()));

    }

    @Test
    void likeFilmFailNotFound() throws Exception {
        Film film = filmService.createFilm(
                new Film("Name1", "Description1", LocalDate.now(), 100, new MPA(1)));

        userService.createUser(
                new User("test1@yandex.ru", "login1", "name1", LocalDate.of(2000,1,1)));


        mockMvc.perform(
                        put("/films/" + film.getId() + "/like/-1"))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException));

    }

    @Test
    void dislikeFilm() throws Exception {
        Film film = filmService.createFilm(
                new Film("Name1", "Description1", LocalDate.now(), 100, new MPA(1)));

        User user = userService.createUser(
                new User("test1@yandex.ru", "login1", "name1", LocalDate.of(2000,1,1)));

        mockMvc.perform(
                        put("/films/" + film.getId() + "/like/" + user.getId()))
                .andExpect(status().isOk());

        Film result = filmService.getFilmById(film.getId());

        assertEquals(1, result.getLikes().size());

        mockMvc.perform(
                        delete("/films/" + film.getId() + "/like/" + user.getId()))
                .andExpect(status().isOk());

        result = filmService.getFilmById(film.getId());

        assertEquals(0, result.getLikes().size());

    }

    @Test
    void dislikeFilmFailNotFound() throws Exception {
        Film film = filmService.createFilm(
                new Film("Name1", "Description1", LocalDate.now(), 100, new MPA(1)));

        User user = userService.createUser(
                new User("test1@yandex.ru", "login1", "name1", LocalDate.of(2000,1,1)));

        mockMvc.perform(
                        put("/films/" + film.getId() + "/like/" + user.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(
                        put("/films/ " + film.getId() + " /like/-1"))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException));

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void getPopular() throws Exception {
        Film film1 = filmService.createFilm(
                new Film("Name1", "Description1", LocalDate.now(), 100, new MPA(1, "G")));
        Film film2 = filmService.createFilm(
                new Film("Name2", "Description2", LocalDate.now(), 200, new MPA(1, "G")));
        Film film3 = filmService.createFilm(
                new Film("Name3", "Description3", LocalDate.now(), 300, new MPA(1, "G")));

        User user1 = userService.createUser(
                new User("test1@yandex.ru", "login1", "name1", LocalDate.of(2000,1,1)));
        User user2 = userService.createUser(
                new User("test2@yandex.ru", "login2", "name2", LocalDate.of(2001,2,2)));
        User user3 = userService.createUser(
                new User("test3@yandex.ru", "login3", "name3", LocalDate.of(2002,3,3)));

        film1 = filmService.likeFilm(film1.getId(), user1.getId());
        filmService.likeFilm(film2.getId(), user1.getId());
        filmService.likeFilm(film2.getId(), user2.getId());
        film2 = filmService.likeFilm(film2.getId(), user3.getId());
        filmService.likeFilm(film3.getId(), user1.getId());
        film3 = filmService.likeFilm(film3.getId(), user2.getId());

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