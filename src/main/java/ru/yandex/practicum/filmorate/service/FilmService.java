package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author kazakov
 * @version 16.09.2022
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;

    public List<Film> getFilms() {
        return filmStorage.findAll();
    }

    public Film createFilm(Film film) {
        validate(film);
        return filmStorage.save(film);
    }

    public Film updateFilm(Film film) {
        filmStorage.findById(film.getId())
                .orElseThrow(() -> new NotFoundException(String.format("Фильм с id = %d не найден",film.getId())));
        validate(film);

        return filmStorage.save(film);
    }

    private void validate(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException(String.format("Дата релиза фильма %s раньше 28.12.1895", film.getReleaseDate()));
        }
    }

    public Film getFilmById(int filmId) {

        return filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException(String.format("Фильм с id = %d не найден", filmId)));
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
