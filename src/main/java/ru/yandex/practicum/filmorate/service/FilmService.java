package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;


@Service
@Slf4j
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("UserDbStorage") UserStorage userStorage) {
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
        filmStorage.addLike(idFilm, userId);
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
        filmStorage.removeLike(idFilm, userId);
    }

    public List<Film> getMostPopularFilms(Integer count) {
        if (count < 0) {
            log.error("Был передан отрицательный count");
            throw new ValidationException("Нельзя передать отрицательное количество фильмов!");
        }
        return filmStorage.getTheMostPopularFilms(count);
    }
}
