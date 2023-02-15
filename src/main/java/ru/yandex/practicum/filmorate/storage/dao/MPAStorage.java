package ru.yandex.practicum.filmorate.storage.dao;

import ru.yandex.practicum.filmorate.model.MPA;

import java.util.List;
import java.util.Optional;

public interface MPAStorage {

    List<MPA> findAll();

    Optional<MPA> findById(long id);

}
