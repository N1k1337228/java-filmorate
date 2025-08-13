package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;


import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public List<Film> getAllFilms() {
        log.info("Вернул список фильмов");
        return filmService.getAllFilms();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Film createFilm(@RequestBody Film film) {
        return filmService.addNewFilm(film);

    }

    @PutMapping
    public Film updateFilm(@RequestBody Film film) {
        return filmService.updateFilm(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public Film addLike(@PathVariable Integer id,@PathVariable Integer userId) {
        return filmService.addLike(id,userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film deleteLike(@PathVariable Integer id,@PathVariable Integer userId) {
        return filmService.removeLike(id,userId);
    }

    @GetMapping("/popular?count={count}")
    public List<Film> getMostPopularFilm(@RequestParam Integer count) {
           return filmService.getMostPopularFilms(count);
    }
}