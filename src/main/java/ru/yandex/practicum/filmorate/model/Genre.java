package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Genre implements Serializable {

    @EqualsAndHashCode.Include
    private long id;
    private String name;

    @JsonCreator
    public Genre(@JsonProperty("id") long id) {
        this.id = id;
    }

    public Genre(long id, String name) {
        this.id = id;
        this.name = name;
    }

}
