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
    public User addFriend(User user, User.Friend friend) {

        String sqlQuery = "insert into user_friends(user_id, friend_id, connection) " +
                "values (?, ?, ?)";
        jdbcTemplate.update(sqlQuery, user.getId(), friend.getFriendId(), friend.getConnectionType().name());
        user.addToFriends(friend);

        return user;
    }

    @Override
    public User updateFriend(User user, User.Friend friend) {

        String sqlQuery = "update user_friends set connection = ? " +
                "where user_friends.user_id = ? and user_friends.friend_id = ?";
        jdbcTemplate.update(sqlQuery, friend.getConnectionType().name(), user.getId(), friend.getFriendId());
        user.addToFriends(friend);

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

        HashMap<Long, Set<User.Friend>> friends = getUsersFriends();

        String sqlQuery = "select id, email, login, name,  birthday from users";
        return jdbcTemplate.queryForStream(sqlQuery, this::mapRowToUser)
                .peek(user -> user.setFriends(friends.getOrDefault(user.getId(), new HashSet<>())))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<User> findById(long id) {

        final Set<User.Friend> friends = getUserFriends(id);

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

        final HashMap<Long, Set<User.Friend>> friends = getUsersFriends(ids);

        String inSql = String.join(",", Collections.nCopies(ids.size(), "?"));
        String sqlQuery = String.format("select id, email, login, name,  birthday " +
                "from users where id IN (%s)", inSql);
        return jdbcTemplate.queryForStream(sqlQuery, this::mapRowToUser, ids.toArray())
                .peek(user -> user.setFriends(friends.get(user.getId())))
                .collect(Collectors.toList());
    }

    private Set<User.Friend> getUserFriends(long id) {

        String sqlQuery = "select friend_id, connection " +
                "from user_friends where user_id = ?";
        return jdbcTemplate.query(sqlQuery, rs -> {
            Set<User.Friend> friend = new HashSet<>();
            while(rs.next()){
                friend.add(new User.Friend(
                        id,
                                rs.getLong("friend_id"),
                                User.ConnectionType.valueOf(rs.getString("connection"))));
            }
            return friend;
        }, id);
    }

    private HashMap<Long, Set<User.Friend>> getUsersFriends() {

        String sqlQuery = "select user_id, friend_id, connection from user_friends";

        return jdbcTemplate.query(
                sqlQuery,
                UserDbStorage::extractFriends);
    }

    private HashMap<Long, Set<User.Friend>> getUsersFriends(Set<Long> ids) {

        String inSql = String.join(",", Collections.nCopies(ids.size(), "?"));
        String sqlQuery = String.format("select user_id, friend_id, connection " +
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

    private static HashMap<Long, Set<User.Friend>> extractFriends(ResultSet rs) throws SQLException {
        HashMap<Long, Set<User.Friend>> map = new HashMap<>();
        while (rs.next()) {
            if (map.containsKey(rs.getLong("user_id"))) {
                map.get(rs.getLong("user_id"))
                        .add(new User.Friend(
                                rs.getLong("user_id"),
                                rs.getLong("friend_id"),
                                User.ConnectionType.valueOf(rs.getString("connection"))));
            } else {
                Set<User.Friend> friend = new HashSet<>();
                friend.add(new User.Friend(
                        rs.getLong("user_id"),
                        rs.getLong("friend_id"),
                        User.ConnectionType.valueOf(rs.getString("connection"))));
                map.put(rs.getLong("user_id"), friend);
            }
        }
        return map;
    }
}
