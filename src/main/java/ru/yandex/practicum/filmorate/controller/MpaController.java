package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.MPA;

import java.util.List;

@RestController
@RequestMapping("/mpa")
public class MpaController {

    @GetMapping
    public List<MPA> getAllMpa() {
        return MPA.getAll();
    }

    @GetMapping("/{id}")
    public MPA getMpaById(@PathVariable int id) {
        return MPA.getById(id);
    }
}

