package ru.yandex.practicum.filmorate.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
class MPAControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void getMPAs() throws Exception {
        mockMvc.perform(
                        get("/mpa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("G"))
                .andExpect(jsonPath("$[1].name").value("PG"))
                .andExpect(jsonPath("$[2].name").value("PG-13"))
                .andExpect(jsonPath("$[3].name").value("R"))
                .andExpect(jsonPath("$[4].name").value("NC-17"));
    }

    @Test
    void getMPA() throws Exception {
        mockMvc.perform(
                        get("/mpa/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("G"));
    }
}