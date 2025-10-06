package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Service
public class MpaService {

    private final List<Mpa> allMpa = List.of(
            new Mpa(1, "G"),
            new Mpa(2, "PG"),
            new Mpa(3, "PG-13"),
            new Mpa(4, "R"),
            new Mpa(5, "NC-17")
    );

    public List<Mpa> getAllMpa() {
        return allMpa;
    }

    public Mpa getMpaById(int id) {
        return allMpa.stream()
                .filter(mpa -> mpa.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("MPA с id=" + id + " не найден"));
    }
}
