package ru.yandex.practicum.filmorate.storage.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.dao.GenreStorage;
import ru.yandex.practicum.filmorate.storage.dao.MPAStorage;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
class MPADbStorageTest {

    @Autowired
    private MPAStorage mpaStorage;

    @Test
    void findAll() {
        List<MPA> result = mpaStorage.findAll();

        assertEquals(5, result.size());
        assertEquals("G", result.get(0).getName());
        assertEquals("PG", result.get(1).getName());
        assertEquals("PG-13", result.get(2).getName());
        assertEquals("R", result.get(3).getName());
        assertEquals("NC-17", result.get(4).getName());
    }

    @Test
    void findById() {
        Optional<MPA> result = mpaStorage.findById(3);

        assertTrue(result.isPresent());
        assertEquals("PG-13", result.get().getName());
    }
}