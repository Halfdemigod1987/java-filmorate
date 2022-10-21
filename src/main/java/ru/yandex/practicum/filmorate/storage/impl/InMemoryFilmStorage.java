package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.dao.FilmStorage;

import java.util.*;

/**
 * @author kazakov
 * @version 16.09.2022
 */
@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Film save(Film film) {
        if (film.getId() == 0) {
            film.setId();
            log.debug("Добавлен фильм: {}", film);
        } else {
            log.debug("Обновлен фильм: {}", film);
        }
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Optional<Film> findById(long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public void addLike(Film film, long userId) {
        film.like(userId);
    }

    @Override
    public void deleteLike(Film film, long userId) {
        film.dislike(userId);
    }
}
