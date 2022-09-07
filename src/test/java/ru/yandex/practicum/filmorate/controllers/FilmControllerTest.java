package ru.yandex.practicum.filmorate.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
class FilmControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void getFilms() throws Exception {
        Film film1 = new Film("Name1", "Description1", LocalDate.now(), 100);
        Film film2 = new Film("Name2", "Description2", LocalDate.now().minusDays(50), 200);
        String list = objectMapper.writeValueAsString(List.of(film1, film2));

        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film1))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film2))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(
                        get("/films"))
                .andExpect(status().isOk())
                .andExpect(content().json(list));
    }

    @Test
    void createFilm() throws Exception {

        LocalDate releaseDate = LocalDate.now();
        Film film = new Film("Name", "Description", releaseDate, 100);
        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(film.getId()))
                .andExpect(jsonPath("$.name").value("Name"))
                .andExpect(jsonPath("$.description").value("Description"))
                .andExpect(jsonPath("$.releaseDate").value(releaseDate.toString()))
                .andExpect(jsonPath("$.duration").value(100));

    }

    @Test
    void createFilmFailName() throws Exception {

        Film film = new Film("", "Description", LocalDate.now(), 100);
        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

    }

    @Test
    void createFilmFailDescription() throws Exception {

        String longName = Stream.generate(() -> "x").limit(400).collect(Collectors.joining());
        Film film = new Film("Name", longName, LocalDate.now(), 100);
        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

    }

    @Test
    void createFilmFailReleaseDate() throws Exception {

        Film film = new Film("Name", "Description", LocalDate.of(1890, 3, 25), 100);
        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof FilmController.FilmValidationException));

    }

    @Test
    void createFilmFailDuration() throws Exception {

        Film film = new Film("Name", "Description", LocalDate.now(), -100);
        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

    }

    @Test
    void createFilmFailAlreadyExists() throws Exception {

        Film film = new Film("Name", "Description", LocalDate.now(), 100);
        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof FilmController.FilmAlreadyExistException));

    }

    @Test
    void updateFilm() throws Exception {

        LocalDate releaseDate = LocalDate.now();
        Film film = new Film("Name", "Description", releaseDate, 100);
        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        film.setName("Updated name");
        film.setDescription("Updated description");
        film.setReleaseDate(releaseDate.minusDays(100));
        film.setDuration(200);
        mockMvc.perform(
                        put("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(film.getId()))
                .andExpect(jsonPath("$.name").value("Updated name"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.releaseDate").value(releaseDate.minusDays(100).toString()))
                .andExpect(jsonPath("$.duration").value(200));

    }

    @Test
    void updateFilmFailNotFound() throws Exception {

        Film film = new Film("Name", "Description", LocalDate.now(), 100);
        mockMvc.perform(
                        put("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof FilmController.FilmNotFoundException));

    }
}