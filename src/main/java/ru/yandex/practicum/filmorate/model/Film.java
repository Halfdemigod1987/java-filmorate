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

    private MPA mpa;

    public Film(String name, String description, LocalDate releaseDate, int duration, MPA mpa) {
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.mpa = mpa;
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

}
