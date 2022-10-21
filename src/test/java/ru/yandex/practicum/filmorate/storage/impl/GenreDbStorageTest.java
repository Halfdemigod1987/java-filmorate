package ru.yandex.practicum.filmorate.storage.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.dao.GenreStorage;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureTestDatabase
class GenreDbStorageTest {

    @Autowired
    private GenreStorage genreStorage;

    @Test
    void findAll() {
        List<Genre> result = genreStorage.findAll();

        assertEquals(6, result.size());
        assertEquals("Комедия", result.get(0).getName());
        assertEquals("Драма", result.get(1).getName());
        assertEquals("Мультфильм", result.get(2).getName());
        assertEquals("Триллер", result.get(3).getName());
        assertEquals("Документальный", result.get(4).getName());
        assertEquals("Боевик", result.get(5).getName());
    }

    @Test
    void findById() {
        Optional<Genre> result = genreStorage.findById(3);

        assertTrue(result.isPresent());
        assertEquals("Мультфильм", result.get().getName());
    }
}