package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    HashMap<Integer, Film> filmMap = new HashMap<>();

    @GetMapping
    public List<Film> getAllFilms() {
        log.info("Вернул список фильмов");
        return new ArrayList<>(filmMap.values());

    }

    @PostMapping
    public Film createFilm(@RequestBody Film film) {
        if (film.getName().isBlank()) {
            log.error("Пустая строка/пробел в названии фильма");
            throw new ValidationException("название не может быть пустым");
        }
        if (film.getDescription().length() > 200) {
            log.error("Описание фильма занимает более 200 символов");
            throw new ValidationException("максимальная длина описания — 200 символов");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("введённая дата релиза фильма раньше 1895 года");
            throw new ValidationException("дата релиза — не раньше 28 декабря 1895 года");
        }
        if (film.getDuration() < 0) {
            log.error("Продолжительность фильма указана, как отрицательное число");
            throw new ValidationException("продолжительность фильма должна быть положительным числом");
        }
        film.setId(getNextId());
        filmMap.put(film.getId(), film);
        log.info("Добавлен фильм {}", film.getName());
        log.debug("Добавлен фильм {}", film.getName());
        return film;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film film) {
        if (film.getId() == null) {
            log.error("Не указан Id фильма");
            throw new ValidationException("Должен быть указан Id фильма");
        }
        if (film.getDescription() == null || film.getDescription().isBlank()) {
            log.error("Не указано описание фильма");
            throw new ValidationException("Должно быть указано описание фильма");
        }
        if (filmMap.containsKey(film.getId())) {
            filmMap.put(film.getId(),film);
            log.info("Фильм с именем {} и Id {} успешно обновлён,обновлённое описание: {}",
                    film.getName(), film.getId(), film.getDescription());
            log.debug("Фильм с именем {} и Id {} успешно обновлён,обновлённое описание: {}",
                    film.getName(), film.getId(), film.getDescription());
            return film;
        }
        log.error("Фильма с Id {} не был найден", film.getId());
        throw new NotFoundException("Фильм не найден");
    }

    private int getNextId() {
        int currentMaxId = filmMap.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}