package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
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

    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film);
    }

    public Film addNewFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public void addLike(Integer idFilm, Integer userId) {
        if (idFilm == null || userId == null) {
            log.error("Полученный id пустые");
            throw new ValidationException("Полученный id пустые");
        }
        if (userStorage.getUserOnId(userId) == null) {
            log.error("Неизвестный пользователь пытался поставить лайк");
            throw new NotFoundException("Незарегистрированный пользователь не может ставить лайки");
        }
        if (filmStorage.getFilmOnId(idFilm) == null) {
            log.error("передан id несуществующего фильма");
            throw new NotFoundException("фильм не найден");
        }
        filmStorage.getFilmOnId(idFilm).setUserOnLikeList(userId);
    }

    public void removeLike(Integer idFilm, Integer userId) {
        if (idFilm == null || userId == null) {
            log.error("полученный id пустые");
            throw new ValidationException("Полученный id пустые");
        }
        if (userStorage.getUserOnId(userId) == null) {
            log.error("неизвестный пользователь пытался удалить лайк");
            throw new NotFoundException("Незарегистрированный пользователь не может удалять лайки");
        }
        if (filmStorage.getFilmOnId(idFilm) == null) {
            log.error("Передан id несуществующего фильма");
            throw new NotFoundException("фильм не найден");
        }
        filmStorage.getFilmOnId(idFilm).removeUserOnLikeList(userId);
    }

    public List<Film> getMostPopularFilms(Integer count) {
        if (count < 0) {
            log.error("Был передан отрицательный count");
            throw new ValidationException("Нельзя передать отрицательное количество фильмов!");
        }
        return filmStorage.getAllFilms().stream()
                .sorted((film1, film2) -> film2.getLikes().compareTo(film1.getLikes()))
                .limit(count)
                .collect(Collectors.toList());
    }
}
