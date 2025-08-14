package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    private boolean isUserInStorage(Integer id) {
        User user = userStorage.getUserOnId(id);
        return userStorage.getAllUsers().contains(user);
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
        if (idFilm == null || userId == null) {
            log.error("Полученный id пустые");
            throw new ValidationException("Полученный id пустые");
        }
        if (!isUserInStorage(userId)) {
            log.error("Неизвестный пользователь пытался поставить лайк");
            throw new NotFoundException("Незарегистрированный пользователь не может ставить лайки");
        }
        Film film = filmStorage.getFilmOnId(idFilm);
        if (film == null) {
            log.error("передан id несуществующего фильма");
            throw new NotFoundException("фильм не найден");
        }
        film.setUserOnLikeList(userId);
        return filmStorage.updateFilm(film);
    }

    public Film removeLike(Integer idFilm, Integer userId) {
        if (idFilm == null || userId == null) {
            log.error("полученный id пустые");
            throw new ValidationException("Полученный id пустые");
        }
        if (!isUserInStorage(userId)) {
            log.error("неизвестный пользователь пытался удалить лайк");
            throw new NotFoundException("Незарегистрированный пользователь не может удалять лайки");
        }
        Film film = filmStorage.getFilmOnId(idFilm);
        if (film == null) {
            log.error("Передан id несуществующего фильма");
            throw new NotFoundException("фильм не найден");
        }
        film.removeUserOnLikeList(userId);
        return filmStorage.updateFilm(film);
    }

    public List<Film> getMostPopularFilms(Integer count) {
        Integer countOfFilms = count;
        if (count == null) {
            countOfFilms = 10;
        }
        List<Film> allFilms = filmStorage.getAllFilms();
        if (allFilms.size() < countOfFilms) {
            countOfFilms = filmStorage.getAllFilms().size();
        }
        return allFilms.stream().sorted((film1, film2) -> film2.getLikes().compareTo(film1.getLikes())).limit(countOfFilms).collect(Collectors.toList());
    }
}
