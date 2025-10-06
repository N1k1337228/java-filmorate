package ru.yandex.practicum.filmorate.storage.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.mapper.FilmMapper;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
@Qualifier("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private static final int MAX_LENGTH_DESCRIPTION = 200;
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private final JdbcTemplate jdbc;
    private final String insertNewFilm = "INSERT INTO films (id, title, description, release_date, likes_count, " +
            "duration, rating) VALUES (?,?,?,?,?,?,?)";
    private final String updateFilm = "UPDATE films SET title=?, description=?, release_date=?, likes_count=?, " +
            "duration=?, rating=? WHERE id = ?";
    private final String deleteFilm = "DELETE FROM films WHERE id = ?";
    private final String findFilmOnId = "SELECT * FROM films WHERE id = ?";

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
        if (film.getId() == null) {
            Integer nextId = jdbc.queryForObject(
                    "SELECT COALESCE(MAX(id), 0) + 1 FROM films",
                    Integer.class
            );
            film.setId(nextId);
        }
        jdbc.update(insertNewFilm, film.getId(), film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getLikes(), film.getDuration(), film.getMpa().getName());
        return film;
    }

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
        jdbc.update(updateFilm, film.getName(), film.getDescription(), film.getReleaseDate(), film.getLikes(),
                film.getDuration(), film.getMpa().getName(), film.getId());
        return film;
    }

    public void removeFilm(Integer filmId) {
        if (jdbc.queryForObject("SELECT title FROM films AS f WHERE id = ?", String.class, filmId) != null) {
            jdbc.update(deleteFilm, filmId);
        }
    }

    public List<Film> getAllFilms() {
        List<Film> films = jdbc.query("SELECT * FROM films", new FilmMapper());
        return getAllFilmsWithLikesAndGenres(films);
    }

    public Film getFilmOnId(Integer id) {
        Film film = jdbc.queryForObject(findFilmOnId, new FilmMapper(), id);
        film.setUsersIdLike(jdbc.queryForList("SELECT user_id FROM like_users WHERE film_id = ?", java.lang.Integer.class, id).stream()
                .collect(Collectors.toSet()));
        return film;
    }

    public void addLike(Integer filmId, Integer userId) {
        try {
            jdbc.queryForObject("SELECT id FROM like_users WHERE film_id = ? AND user_id = ?",
                    Integer.class, filmId, userId);
        } catch (EmptyResultDataAccessException e) {
            jdbc.update("UPDATE films SET likes_count = likes_count + 1 WHERE id = ?", filmId);
            jdbc.update("INSERT INTO like_users (film_id, user_id) VALUES(?,?)", filmId, userId);
        }
    }

    public void removeLike(Integer filmId, Integer userId) {
        try {
            jdbc.queryForObject("SELECT id FROM like_users WHERE film_id = ? AND user_id = ?",
                    Integer.class, filmId, userId);
            jdbc.update("DELETE FROM like_users WHERE film_id = ? AND user_id = ?", filmId, userId);
            jdbc.update("UPDATE films SET likes_count = likes_count - 1 WHERE id = ?", filmId);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Лайк не может быть удалён так как пользователь его не ставил");
        }
    }

    public List<Film> getTheMostPopularFilms(Integer count) {
        List<Film> films = jdbc.query("SELECT * FROM films " +
                "ORDER BY likes_count DESC LIMIT ?", new FilmMapper(), count);
        if (films.isEmpty()) {
            return new ArrayList<Film>();
        }
        return getAllFilmsWithLikesAndGenres(films);
    }

    public List<Film> getAllFilmsWithLikesAndGenres(List<Film> films) {
        if (films.isEmpty()) {
            return films;
        }
        Map<Integer, Set<Integer>> likesByFilm = getAllLikes();
        Map<Integer, List<Genre>> genresByFilm = getAllGenres();
        for (Film film : films) {
            film.setUsersIdLike(likesByFilm.getOrDefault(film.getId(), Set.of()));
            film.setGenreOfFilm(genresByFilm.getOrDefault(film.getId(), List.of()));
        }
        return films;
    }

    private Map<Integer, Set<Integer>> getAllLikes() {
        return jdbc.query(
                "SELECT film_id, user_id FROM like_users",
                rs -> {
                    Map<Integer, Set<Integer>> map = new HashMap<>();
                    while (rs.next()) {
                        int filmId = rs.getInt("film_id");
                        int userId = rs.getInt("user_id");
                        map.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
                    }
                    return map;
                }
        );
    }

    private Map<Integer, List<Genre>> getAllGenres() {
        return jdbc.query(
                "SELECT fg.film_id, g.id AS genre_id, g.name AS genre_name " +
                        "FROM film_genre fg " +
                        "JOIN genre g ON fg.genre_id = g.id",
                rs -> {
                    Map<Integer, List<Genre>> map = new HashMap<>();
                    while (rs.next()) {
                        int filmId = rs.getInt("film_id");
                        int genreId = rs.getInt("genre_id");
                        String genreName = rs.getString("genre_name");
                        Genre genre = new Genre();
                        genre.setId(genreId);
                        genre.setName(genreName);
                        List<Genre> genres = map.get(filmId);
                        if (genres == null) {
                            genres = new ArrayList<>();  // ← ArrayList вместо HashSet
                            map.put(filmId, genres);
                        }
                        genres.add(genre);
                    }
                    return map;
                }
        );
    }
}
