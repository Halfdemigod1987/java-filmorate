package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dao.UserStorage;

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
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User save(User user) {

        KeyHolder keyHolder = new GeneratedKeyHolder();

        if (user.getId() == 0) {
            log.debug("Добавлен пользователь: {}", user);

            String sqlQuery = "insert into users(email, login, name, birthday) " +
                    "values (?, ?, ?, ?)";

            jdbcTemplate.update(connection -> {
                PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"id"});
                stmt.setString(1, user.getEmail());
                stmt.setString(2, user.getLogin());
                stmt.setString(3, user.getName());
                stmt.setTimestamp(4, Timestamp.valueOf(user.getBirthday().atStartOfDay()));
                return stmt;
            }, keyHolder);

            user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());

        } else {
            log.debug("Обновлен пользователь: {}", user);

            String sqlQuery = "update users set email = ?, login = ?, name = ?, birthday = ? " +
                    "where users.id = ?";

            jdbcTemplate.update(sqlQuery,
                    user.getEmail(),
                    user.getLogin(),
                    user.getName(),
                    user.getBirthday(),
                    user.getId());
        }

        return user;

    }

    @Override
    public User addFriend(User user, long friendId, User.Connection connection) {

        String sqlQuery = "insert into user_friends(user_id, friend_id, connection_id) " +
                "values (?, ?, ?)";
        jdbcTemplate.update(sqlQuery, user.getId(), friendId, connection.name());
        user.addToFriends(friendId, connection);

        return user;
    }

    @Override
    public User updateFriend(User user, long friendId, User.Connection connection) {

        String sqlQuery = "update user_friends set connection_id = ? " +
                "where user_friends.user_id = ? and user_friends.friend_id = ?";
        jdbcTemplate.update(sqlQuery, connection.name(), user.getId(), friendId);
        user.addToFriends(friendId, connection);

        return user;
    }

    @Override
    public User deleteFriend(User user, long friendId) {

        String sqlQuery = "delete from user_friends where user_id = ? and friend_id = ? ";
        jdbcTemplate.update(sqlQuery, user.getId(), friendId);
        user.deleteFromFriends(friendId);

        return user;
    }

    @Override
    public List<User> findAll() {

        HashMap<Long, HashMap<Long, User.Connection>> friends = getUsersFriends();

        String sqlQuery = "select id, email, login, name,  birthday from users";
        return jdbcTemplate.queryForStream(sqlQuery, this::mapRowToUser)
                .peek(user -> user.setFriends(friends.getOrDefault(user.getId(), new HashMap<>())))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<User> findById(long id) {

        final HashMap<Long, User.Connection> friends = getUserFriends(id);

        String sqlQuery = "select id, email, login, name,  birthday " +
                "from users where id = ?";

        return jdbcTemplate.queryForStream(sqlQuery, this::mapRowToUser, id)
                .peek(result -> result.setFriends(friends))
                .findFirst();
    }

    @Override
    public List<User> findById(Set<Long> ids) {
        if (ids.size() == 0) {
            return new ArrayList<>();
        }

        final HashMap<Long, HashMap<Long, User.Connection>> friends = getUsersFriends(ids);

        String inSql = String.join(",", Collections.nCopies(ids.size(), "?"));
        String sqlQuery = String.format("select id, email, login, name,  birthday " +
                "from users where id IN (%s)", inSql);
        return jdbcTemplate.queryForStream(sqlQuery, this::mapRowToUser, ids.toArray())
                .peek(user -> user.setFriends(friends.get(user.getId())))
                .collect(Collectors.toList());
    }

    private HashMap<Long, User.Connection> getUserFriends(long id) {

        String sqlQuery = "select friend_id, connection_id " +
                "from user_friends where user_id = ?";
        return jdbcTemplate.query(sqlQuery, rs -> {
            HashMap<Long, User.Connection> map = new HashMap<>();
            while(rs.next()){
                map.put(rs.getLong("friend_id"),
                        User.Connection.valueOf(rs.getString("connection_id")));
            }
            return map;
        }, id);
    }

    private HashMap<Long, HashMap<Long, User.Connection>> getUsersFriends() {

        String sqlQuery = "select user_id, friend_id, connection_id from user_friends";

        return jdbcTemplate.query(
                sqlQuery,
                UserDbStorage::extractFriends);
    }

    private HashMap<Long, HashMap<Long, User.Connection>> getUsersFriends(Set<Long> ids) {

        String inSql = String.join(",", Collections.nCopies(ids.size(), "?"));
        String sqlQuery = String.format("select user_id, friend_id, connection_id " +
                "from user_friends where user_id IN (%s)", inSql);

        return jdbcTemplate.query(
                sqlQuery,
                UserDbStorage::extractFriends,
                ids.toArray());
    }

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        int id = rs.getInt("id");
        String email = rs.getString("email");
        String login = rs.getString("login");
        String name  = rs.getString("name");
        LocalDate birthday = rs.getDate("birthday").toLocalDate();
        User user = new User(email, login, name, birthday);
        user.setId(id);
        return user;
    }

    private static HashMap<Long, HashMap<Long, User.Connection>> extractFriends(ResultSet rs) throws SQLException {
        HashMap<Long, HashMap<Long, User.Connection>> map = new HashMap<>();
        while (rs.next()) {
            if (map.containsKey(rs.getLong("user_id"))) {
                map.get(rs.getLong("user_id"))
                        .put(
                                rs.getLong("friend_id"),
                                User.Connection.valueOf(rs.getString("connection_id")));
            } else {
                HashMap<Long, User.Connection> friend = new HashMap<>();
                friend.put(
                        rs.getLong("friend_id"),
                        User.Connection.valueOf(rs.getString("connection_id")));
                map.put(rs.getLong("user_id"), friend);
            }
        }
        return map;
    }
}
