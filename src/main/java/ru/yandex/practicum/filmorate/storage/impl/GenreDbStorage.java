package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.dao.GenreStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Genre> findAll() {
        String sqlQuery = "select id, name from genres";
        return jdbcTemplate.query(sqlQuery, this::mapRowToGenre);
    }

    @Override
    public Optional<Genre> findById(long id) {
        String sqlQuery = "select id, name " +
                "from genres where id = ?";
        return jdbcTemplate.queryForStream(sqlQuery, this::mapRowToGenre, id).findFirst();
    }

    private Genre mapRowToGenre(ResultSet rs, int i) throws SQLException {
        long id = rs.getLong("id");
        String name = rs.getString("name");
        return new Genre(id, name);
    }
}
