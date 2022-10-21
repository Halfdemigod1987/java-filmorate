package ru.yandex.practicum.filmorate.storage.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dao.FilmStorage;
import ru.yandex.practicum.filmorate.storage.dao.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureTestDatabase
class FilmDbStorageTest {
    @Autowired
    private FilmStorage filmStorage;
    @Autowired
    private UserStorage userStorage;

    @Test
    void save() {

        Film film = filmStorage.save(
                new Film("Name1", "Description1", LocalDate.now(), 100, new MPA(1, "G")));

        Optional<Film> result = filmStorage.findById(film.getId());

        assertTrue(result.isPresent());
        assertEquals(film.getName(), result.get().getName());
        assertEquals(film.getDescription(), result.get().getDescription());
        assertEquals(film.getReleaseDate(), result.get().getReleaseDate());
        assertEquals(film.getDuration(), result.get().getDuration());
        assertEquals(film.getMpa().getName(), result.get().getMpa().getName());

        film.setName("Updated name");
        film.setDescription("Updated description");
        film.setReleaseDate(LocalDate.now().minusDays(100));
        film.setDuration(200);
        film.setMpa(new MPA(2, "PG"));

        filmStorage.save(film);

        result = filmStorage.findById(film.getId());

        assertEquals(film.getName(), result.get().getName());
        assertEquals(film.getDescription(), result.get().getDescription());
        assertEquals(film.getReleaseDate(), result.get().getReleaseDate());
        assertEquals(film.getDuration(), result.get().getDuration());
        assertEquals(film.getMpa().getName(), result.get().getMpa().getName());
    }

    @Test
    void addLike() {
        Film film = filmStorage.save(
                new Film("Name1", "Description1", LocalDate.now(), 100, new MPA(1, "G")));
        User user = userStorage.save(
                new User("test1@yandex.ru", "login1", "name1", LocalDate.of(2000,1,1)));

        filmStorage.addLike(film, user.getId());

        Optional<Film> result = filmStorage.findById(film.getId());

        assertEquals(1, result.get().getLikes().size());
        assertTrue(result.get().getLikes().contains(user.getId()));

    }

    @Test
    void deleteLike() {
        Film film = filmStorage.save(
                new Film("Name1", "Description1", LocalDate.now(), 100, new MPA(1, "G")));
        User user = userStorage.save(
                new User("test1@yandex.ru", "login1", "name1", LocalDate.of(2000,1,1)));

        filmStorage.addLike(film, user.getId());
        filmStorage.deleteLike(film, user.getId());

        Optional<Film> result = filmStorage.findById(film.getId());

        assertEquals(0, result.get().getLikes().size());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void findAll() {

        Film film1 = filmStorage.save(
                new Film("Name1", "Description1", LocalDate.now(), 100, new MPA(1, "G")));
        Film film2 = filmStorage.save(
                new Film("Name2", "Description2", LocalDate.now().minusDays(50), 200, new MPA(2, "PG")));

        List<Film> result = filmStorage.findAll();

        assertEquals(2, result.size());
        assertTrue(result.containsAll(List.of(film1, film2)));
    }

    @Test
    void findById() {

        Film film = filmStorage.save(new Film("Name1", "Description1", LocalDate.now(), 100, new MPA(1, "G")));

        Optional<Film> result = filmStorage.findById(film.getId());

        assertTrue(result.isPresent());
        assertEquals(film.getName(), result.get().getName());
        assertEquals(film.getDescription(), result.get().getDescription());
        assertEquals(film.getReleaseDate(), result.get().getReleaseDate());
        assertEquals(film.getDuration(), result.get().getDuration());
        assertEquals(film.getMpa().getName(), result.get().getMpa().getName());

    }
}