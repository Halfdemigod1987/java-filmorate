package ru.yandex.practicum.filmorate.storage.dao;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

/**
 * @author kazakov
 * @version 16.09.2022
 */
public interface FilmStorage {

    Film save(Film film);

    List<Film> findAll();

    Optional<Film> findById(long id);

    void addLike(Film film, long userId);

    void deleteLike(Film film, long userId);

}
