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

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class FilmService {

    private static final int MAX_LENGTH_DESCRIPTION = 200;
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("UserDbStorage") UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;


    }

    public Film updateFilm(Film film) {
        if (film == null) {
            throw new ValidationException("пустое тело запроса");
        }
        if (film.getId() == null) {
            log.error("Не указан Id фильма");
            throw new ValidationException("Должен быть указан Id фильма");
        }
        if (film.getMpa() == null || film.getMpa().getId() == null) {
            throw new ValidationException("MPA-рейтинг должен быть указан");
        }
        if (film.getDescription() == null || film.getDescription().isBlank() ||
                film.getDescription().length() > MAX_LENGTH_DESCRIPTION) {
            log.error("Не указано описание фильма");
            throw new ValidationException("Должно быть указано описание фильма");
        }
        if (film.getName() == null || film.getName().isBlank()) {
            log.error("пустая строка/пробел в названии фильма");
            throw new ValidationException("название не может быть пустым");
        }
        if (film.getDuration() <= 0) {
            log.error("продолжительность фильма указана, как отрицательное число");
            throw new ValidationException("продолжительность фильма должна быть положительным числом");
        }
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.error("Введённая дата релиза фильма раньше 1895 года");
            throw new ValidationException("дата релиза — не раньше 28 декабря 1895 года");
        }
        return filmStorage.updateFilm(film);
    }

    public Film addNewFilm(Film film) {

        if (film == null) {
            throw new ValidationException("пустое тело запроса");
        }
        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Пустая строка/пробел в названии фильма");
            throw new ValidationException("название не может быть пустым");
        }
        if (film.getMpa() == null || film.getMpa().getId() == null) {
            throw new ValidationException("MPA-рейтинг должен быть указан");
        }
        if (film.getDescription() == null || film.getDescription().length() > MAX_LENGTH_DESCRIPTION) {
            log.error("Описание фильма занимает более 200 символов");
            throw new ValidationException("максимальная длина описания — 200 символов");
        }
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.error("введённая дата релиза фильма раньше 1895 года");
            throw new ValidationException("дата релиза — не раньше 28 декабря 1895 года");
        }
        if (film.getDuration() <= 0) {
            log.error("Продолжительность фильма указана, как отрицательное число");
            throw new ValidationException("продолжительность фильма должна быть положительным числом");
        }
        return filmStorage.addFilm(film);
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public void addLike(int idFilm, int userId) {
        if (userStorage.getUserOnId(userId) == null) {
            log.error("Неизвестный пользователь пытался поставить лайк");
            throw new NotFoundException("Незарегистрированный пользователь не может ставить лайки");
        }
        if (filmStorage.getFilmOnId(idFilm) == null) {
            log.error("Попытка поставить лайк несуществующему фильму");
            throw new NotFoundException("Фильм не найден");
        }
        filmStorage.addLike(idFilm, userId);
    }

    public void removeLike(int idFilm, int userId) {
        if (userStorage.getUserOnId(userId).isEmpty()) {
            log.error("неизвестный пользователь пытался удалить лайк");
            throw new NotFoundException("Незарегистрированный пользователь не может удалять лайки");
        }
        filmStorage.removeLike(idFilm, userId);
    }

    public Film getFilmOnId(int id) {
        return filmStorage.getFilmOnId(id);
    }

    public List<Film> getMostPopularFilms(int count) {
        if (count < 0) {
            log.error("Был передан отрицательный count");
            throw new ValidationException("Нельзя передать отрицательное количество фильмов!");
        }
        return filmStorage.getTheMostPopularFilms(count);
    }
}
