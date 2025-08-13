package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
public class FilmService {

    private final FilmStorage filmStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film);
    }

    public Film addNewFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film addLike(Integer idFilm, Integer userId) {
        Film film = filmStorage.getFilmOnId(idFilm);
        film.setUserOnLikeList(userId);
        return film;
    }

    public Film removeLike(Integer idFilm, Integer userId) {
        Film film = filmStorage.getFilmOnId(idFilm);
        film.removeUserOnLikeList(userId);
        return film;
    }

    public List<Film> getMostPopularFilms(Integer count) {
        Integer countOfFilms = count;
        if (count == null) {
            countOfFilms = 10;
        }
        return filmStorage.getAllFilms().stream()
                .sorted((Film film1, Film film2) ->
                        film1.getLikes().compareTo(film2.getLikes()))
                .limit(countOfFilms)
                .collect(Collectors.toList());
    }
}
