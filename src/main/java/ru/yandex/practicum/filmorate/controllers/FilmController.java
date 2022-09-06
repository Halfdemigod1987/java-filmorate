package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private final Set<Film> films = new HashSet<>();

    @GetMapping
    public Set<Film> getFilms(HttpServletRequest request) {
        return films;
    }

    @PostMapping
    public Film createFilm(HttpServletRequest request, @Valid @RequestBody Film film) {
        if (films.contains(film)) {
            log.warn("Уже существует фильм: {}", film);
            throw new FilmAlreadyExistException();
        }
        validate(film);
        films.add(film);
        log.debug("Добавлен фильм: {}", film);
        return film;
    }

    @PutMapping
    public Film updateFilm(HttpServletRequest request, @Valid @RequestBody Film film) {
        if (!films.contains(film)) {
            log.warn("Не найден фильм: {}", film);
            throw new FilmNotFoundException();
        }
        validate(film);
        films.remove(film);
        films.add(film);
        log.debug("Обновлен фильм: {}", film);
        return film;
    }

    private void validate(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("Дата релиза фильма раньше 28.12.1895: {}", film);
            throw new FilmValidationException();
        }
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public static class FilmAlreadyExistException extends RuntimeException {}

    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public static class FilmNotFoundException extends RuntimeException {}

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public static class FilmValidationException extends RuntimeException {}

}
