package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {
    private static AtomicLong USER_ID = new AtomicLong();

    @EqualsAndHashCode.Include
    private long id;
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String login;
    private String name;
    @PastOrPresent
    private LocalDate birthday;
    private HashMap<Long, Connection> friends = new HashMap<>();

    public User(String email, String login, String name, LocalDate birthday) {
        this.email = email;
        this.login = login;
        if (name == null || name.isBlank()) {
            this.name = login;
        } else {
            this.name = name;
        }
        this.birthday = birthday;
    }

    public void setId() {
        id = USER_ID.incrementAndGet();
    }

    public void addToFriends(long id, Connection connection) {
        friends.put(id, connection);
    }

    public void deleteFromFriends(long id) {
        friends.remove(id);
    }

    public enum Connection {
        Unconfirmed("неподтверждённая "),
        Confirmed("подтверждённая ");

        private final String synonym;

        Connection(String s) {
            synonym = s;
        }

        public String toString() {
            return this.synonym;
        }
    }
}
