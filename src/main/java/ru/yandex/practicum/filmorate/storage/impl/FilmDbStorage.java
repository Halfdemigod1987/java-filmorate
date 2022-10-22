package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.dao.FilmStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@Slf4j
@Primary
public class FilmDbStorage implements FilmStorage {

    JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    @Override
    public Film save(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        if (film.getId() == 0) {
            log.debug("Добавлен фильм: {}", film);

            String sqlQuery = "insert into films(name, description, release_date, duration, rating_mpa) " +
                    "values (?, ?, ?, ?, ?)";

            jdbcTemplate.update(connection -> {
                PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"id"});
                stmt.setString(1, film.getName());
                stmt.setString(2, film.getDescription());
                stmt.setTimestamp(3, Timestamp.valueOf(film.getReleaseDate().atStartOfDay()));
                stmt.setInt(4, film.getDuration());
                stmt.setLong(5, film.getMpa().getId());
                return stmt;
            }, keyHolder);

            film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());

        } else {
            log.debug("Обновлен фильм: {}", film);

            String sqlQuery = "update films set name = ?, description = ?, release_date = ?, duration = ?, rating_mpa = ? " +
                    "where films.id = ?";

            jdbcTemplate.update(sqlQuery,
                    film.getName(),
                    film.getDescription(),
                    film.getReleaseDate(),
                    film.getDuration(),
                    film.getMpa().getId(),
                    film.getId());
        }

        updateGenres(film);

        return film;
    }

    private void updateGenres(Film film) {

        String sqlQueryDelete = "delete from film_genres where film_id = ?";
        jdbcTemplate.update(sqlQueryDelete, film.getId());

        if (film.getGenres().size() > 0) {
            Iterator<Genre> iter = film.getGenres().iterator();

            String sqlQueryInsert = "insert into film_genres (film_id, genre_id) values(?,?)";
            jdbcTemplate.batchUpdate(
                    sqlQueryInsert,
                    new BatchPreparedStatementSetter() {

                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            Genre genre = iter.next();
                            ps.setLong(1, film.getId());
                            ps.setLong(2, genre.getId());
                        }

                        public int getBatchSize() {
                            return film.getGenres().size();
                        }

                    });
        }
    }

    @Override
    public void addLike(Film film, long userId) {

        String sqlQuery = "insert into film_likes(film_id, user_id) " +
                "values (?, ?)";
        jdbcTemplate.update(sqlQuery, film.getId(), userId);
        film.like(userId);

    }

    @Override
    public void deleteLike(Film film, long userId) {

        String sqlQuery = "delete from film_likes where film_id = ? and user_id = ? ";
        jdbcTemplate.update(sqlQuery, film.getId(), userId);
        film.dislike(userId);

    }

    @Override
    public List<Film> findAll() {
        final HashMap<Long, Set<Genre>> genres = getFilmsGenres();
        final HashMap<Long, Set<Long>> likes = getFilmsLikes();

        String sqlQuery = "select films.id, films.name, films.description, films.release_date, films.duration, " +
                "ratings_mpa.id as ratings_mpa_id, ratings_mpa.name as ratings_mpa_name " +
                "from films left join ratings_mpa " +
                "on films.rating_mpa = ratings_mpa.id";
        return jdbcTemplate.queryForStream(sqlQuery, this::mapRowToFilm)
                .peek(film -> {
                    film.setGenres(genres.getOrDefault(film.getId(), new HashSet<>()));
                    film.setLikes(likes.getOrDefault(film.getId(), new HashSet<>()));
                })
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Film> findById(long id) {
        final Set<Genre> genres = getFilmGenres(id);
        final Set<Long> likes = getFilmLikes(id);

        String sqlQuery = "select films.id, films.name, films.description, films.release_date, films.duration, " +
                "ratings_mpa.id as ratings_mpa_id, ratings_mpa.name as ratings_mpa_name " +
                "from films left join ratings_mpa " +
                "on films.rating_mpa = ratings_mpa.id " +
                "where films.id = ?";
        return jdbcTemplate.queryForStream(sqlQuery, this::mapRowToFilm, id)
                .peek(film -> {
                    film.setGenres(genres);
                    film.setLikes(likes);
                })
                .findFirst();
    }

    private Set<Genre> getFilmGenres(long id) {

        String sqlQuery = "select film_genres.genre_id as id, genres.name " +
                "from film_genres left join genres " +
                "on film_genres.genre_id = genres.id " +
                "where film_id = ?";
        return jdbcTemplate.queryForStream(
                    sqlQuery,
                    (rs, rowNum) -> new Genre(rs.getLong("id"), rs.getString("name")),
                    id)
                .collect(Collectors.toSet());
    }

    private HashMap<Long, Set<Genre>> getFilmsGenres() {

        String sqlQuery = "select film_genres.film_id, film_genres.genre_id as id, genres.name as genre_name " +
                "from film_genres left join genres " +
                "on film_genres.genre_id = genres.id";

        return jdbcTemplate.query(
                sqlQuery,
                FilmDbStorage::extractGenres);
    }

    private Set<Long> getFilmLikes(long id) {

        String sqlQuery = "select user_id from film_likes where film_id = ?";
        return jdbcTemplate.queryForStream(
                        sqlQuery,
                        (rs, rowNum) -> rs.getLong("user_id"),
                        id)
                .collect(Collectors.toSet());
    }

    private HashMap<Long, Set<Long>> getFilmsLikes() {

        String sqlQuery = "select film_id, user_id from film_likes";

        return jdbcTemplate.query(
                sqlQuery,
                FilmDbStorage::extractLikes);
    }

    private Film mapRowToFilm(ResultSet rs, int i) throws SQLException {
        long id = rs.getLong("id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        int duration = rs.getInt("duration");
        LocalDate releaseDate = rs.getDate("release_date").toLocalDate();
        MPA mpa = new MPA(rs.getLong("ratings_mpa_id"), rs.getString("ratings_mpa_name"));
        Film film = new Film(name, description, releaseDate, duration, mpa);
        film.setId(id);
        return film;
    }

    private static HashMap<Long, Set<Genre>> extractGenres(ResultSet rs) throws SQLException {
        HashMap<Long, Set<Genre>> map = new HashMap<>();
        while (rs.next()) {
            if (map.containsKey(rs.getLong("film_id"))) {
                map.get(rs.getLong("film_id"))
                        .add(new Genre(rs.getLong("genre_id"), rs.getString("genre_name")));
            } else {
                Set<Genre> genre = new HashSet<>();
                genre.add(new Genre(rs.getLong("genre_id"), rs.getString("genre_name")));
                map.put(rs.getLong("film_id"), genre);
            }
        }
        return map;
    }

    private static HashMap<Long, Set<Long>> extractLikes(ResultSet rs) throws SQLException {
        HashMap<Long, Set<Long>> map = new HashMap<>();
        while (rs.next()) {
            if (map.containsKey(rs.getLong("film_id"))) {
                map.get(rs.getLong("film_id"))
                        .add(rs.getLong("user_id"));
            } else {
                Set<Long> like = new HashSet<>();
                like.add(rs.getLong("user_id"));
                map.put(rs.getLong("film_id"), like);
            }
        }
        return map;
    }
}
