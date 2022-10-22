package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.dao.MPAStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
public class MPADbStorage implements MPAStorage {

    private final JdbcTemplate jdbcTemplate;

    public MPADbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<MPA> findAll() {
        String sqlQuery = "select id, name from ratings_mpa";
        return jdbcTemplate.query(sqlQuery, this::mapRowToRating);
    }

    @Override
    public Optional<MPA> findById(long id) {
        String sqlQuery = "select id, name " +
                "from ratings_mpa where id = ?";
        return jdbcTemplate.queryForStream(sqlQuery, this::mapRowToRating, id).findFirst();
    }

    private MPA mapRowToRating(ResultSet rs, int i) throws SQLException {
        long id = rs.getLong("id");
        String name = rs.getString("name");
        MPA mpa = new MPA(id);
        mpa.setName(name);
        return mpa;
    }
}
