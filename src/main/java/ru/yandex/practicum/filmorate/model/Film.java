package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Film {
    private static AtomicInteger FILM_ID = new AtomicInteger();

    @EqualsAndHashCode.Include
    private int id = FILM_ID.incrementAndGet();
    @NotBlank
    private String name;
    @Size(max=200)
    private String description;
    private LocalDate releaseDate;
    @Positive
    private int duration;

    public Film(String name, String description, LocalDate releaseDate, int duration) {
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
    }
}
