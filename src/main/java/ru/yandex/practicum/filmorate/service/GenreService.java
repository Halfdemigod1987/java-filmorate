package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.dao.GenreStorage;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GenreService {

    private final GenreStorage genreStorage;

    public List<Genre> getGenres() {
        return genreStorage.findAll();
    }

    public Genre getGenreById(int genreId) {
        return genreStorage.findById(genreId)
                .orElseThrow(() -> new NotFoundException(String.format("Жанр с id = %d не найден", genreId)));
    }
}
