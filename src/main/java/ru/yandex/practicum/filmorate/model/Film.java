package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Film {
    private static AtomicLong FILM_ID = new AtomicLong();

    @EqualsAndHashCode.Include
    private long id;
    @NotBlank
    private String name;
    @Size(max=200)
    private String description;
    private LocalDate releaseDate;
    @Positive
    private int duration;
    private Set<Long> likes = new HashSet<>();

    private Set<Genre> genres = new HashSet<>();

    private RatingMPA ratingMPA;

    public Film(String name, String description, LocalDate releaseDate, int duration) {
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
    }

    public void setId() {
        id = FILM_ID.incrementAndGet();
    }

    public void like(long id) {
        likes.add(id);
    }

    public void dislike(long id) {
        likes.remove(id);
    }

    public void addGenre(Genre genre) {
        genres.add(genre);
    }

    public void removeGenre(Genre genre) {
        genres.remove(genre);
    }

    public enum RatingMPA {
        G("G"), PG("PG") ,PG13("PG-13"), R("R") ,NC17("NC-17");

        private final String synonym;

        RatingMPA(String s) {
            synonym = s;
        }

        public String toString() {
            return this.synonym;
        }
    }

    public enum Genre {
        Comedy("Комедия"),
        Dram("Драма"),
        Animation("Мультфильм"),
        Thriller("Триллер"),
        Documentary("Документальный"),
        Action("Боевик");

        private final String synonym;

        Genre(String s) {
            synonym = s;
        }

        public String toString() {
            return this.synonym;
        }
    }
}
