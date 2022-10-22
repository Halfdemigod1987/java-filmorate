package ru.yandex.practicum.filmorate.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.service.MPAService;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@Slf4j
@RequiredArgsConstructor
public class MPAController {

    private final MPAService mpaService;

    @GetMapping
    public List<MPA> getMPAs() {
        return mpaService.getMPA();
    }

    @GetMapping("/{id}")
    public MPA getMPA(@PathVariable int id) {
        return mpaService.getMPAById(id);
    }

}
