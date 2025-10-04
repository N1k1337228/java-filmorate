package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private static final int MAX_LENGTH_DESCRIPTION = 200;
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private final HashMap<Integer, Film> filmMap = new HashMap<>();

    @Override
    public Film addFilm(Film film) {
        if (film == null) {
            throw new ValidationException("пустое тело запроса");
        }
        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Пустая строка/пробел в названии фильма");
            throw new ValidationException("название не может быть пустым");
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
        film.setId(getNextId());
        filmMap.put(film.getId(), film);
        log.info("Добавлен фильм {}", film.getName());
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (film == null) {
            throw new ValidationException("пустое тело запроса");
        }
        if (film.getId() == null) {
            log.error("Не указан Id фильма");
            throw new ValidationException("Должен быть указан Id фильма");
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
        if (filmMap.containsKey(film.getId())) {
            filmMap.put(film.getId(), film);
            log.info("Фильм с именем {} и Id {}",
                    film.getName(), film.getId());
            return film;
        }
        log.error("Фильма с Id {} не был найден", film.getId());
        throw new NotFoundException("Фильм не найден");
    }

    @Override
    public void removeFilm(Integer filmId) {
        if (filmId == null) {
            log.error("");
            throw new ValidationException("");
        }
        filmMap.remove(filmId);
    }

    @Override
    public List<Film> getAllFilms() {
        return new ArrayList<>(filmMap.values());
    }

    @Override
    public Film getFilmOnId(Integer id) {
        return filmMap.get(id);
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
