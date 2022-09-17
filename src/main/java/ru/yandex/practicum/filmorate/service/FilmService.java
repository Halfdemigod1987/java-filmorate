package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author kazakov
 * @version 16.09.2022
 */
@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;

    public FilmService(FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public List<Film> getFilms() {
        return filmStorage.findAll();
    }

    public Film createFilm(Film film) {
        validate(film);
        return filmStorage.save(film);
    }

    public Film updateFilm(Film film) {
        Optional<Film> optionalFilm = filmStorage.findById(film.getId());
        if (optionalFilm.isEmpty()) {
            log.warn("Не найден фильм: {}", film);
            throw new NotFoundException(String.format("Фильм с id = %d не найден для обновления",film.getId()));
        }
        validate(film);

        return filmStorage.save(film);
    }

    private void validate(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException(String.format("Дата релиза фильма %s раньше 28.12.1895", film.getReleaseDate()));
        }
    }

    public Film getFilmById(int filmId) {
        Optional<Film> optionalFlm = filmStorage.findById(filmId);
        if (optionalFlm.isEmpty()) {
            throw new NotFoundException(String.format("Фильм с id = %d не найден для получения", filmId));
        }
        return optionalFlm.get();
    }

    public void likeFilm(int filmId, int userId) {
        Film film = getFilmById(filmId);
        User user = userService.getUserById(userId);
        film.like(user.getId());
        updateFilm(film);
    }

    public void dislikeFilm(int filmId, int userId) {
        Film film = getFilmById(filmId);
        User user = userService.getUserById(userId);
        film.dislike(user.getId());
        updateFilm(film);
    }

    public List<Film> getPopular(int count) {
        return filmStorage.findAll().stream()
                .sorted((film1, film2) -> Integer.compare(film2.getLikes().size(), film1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }
}
