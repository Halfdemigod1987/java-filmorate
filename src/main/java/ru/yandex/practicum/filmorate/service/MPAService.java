package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.dao.MPAStorage;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MPAService {

    private final MPAStorage mpaStorage;

    public List<MPA> getMPA() {
        return mpaStorage.findAll();
    }

    public MPA getMPAById(int ratingId) {
        return mpaStorage.findById(ratingId)
                .orElseThrow(() -> new NotFoundException(String.format("Рейтинг с id = %d не найден", ratingId)));
    }
}
