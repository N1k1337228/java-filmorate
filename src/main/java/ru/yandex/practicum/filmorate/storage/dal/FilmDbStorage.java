package ru.yandex.practicum.filmorate.storage.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.mapper.FilmMapper;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@Qualifier("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbc;
    private final String insertNewFilm = "INSERT INTO films VALUES (?,?,?,?,?,?,?)";
    private final String updateFilm = "UPDATE films SET title=?, description=?, release_date=?, likes_count=?, " +
            "duration=?, rating=?";
    private final String deleteFilm = "DELETE FROM films WHERE id = ?";
    private final String findFilmOnId = "SELECT * FROM films WHERE id = ?";

    public Film addFilm(Film film) {
        jdbc.update(insertNewFilm, film.getId(), film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getLikes(), film.getDuration(), film.getRaiting());
        return film;
    }

    public Film updateFilm(Film film) {
        jdbc.update(updateFilm, film.getName(), film.getDescription(), film.getReleaseDate(), film.getLikes(),
                film.getDuration(), film.getRaiting());
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
        Map<Integer, Set<String>> genresByFilm = getAllGenres();
        for (Film film : films) {
            film.setUsersIdLike(likesByFilm.getOrDefault(film.getId(), Set.of()));
            film.setGenreOfFilm(genresByFilm.getOrDefault(film.getId(), Set.of()));
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

    private Map<Integer, Set<String>> getAllGenres() {
        return jdbc.query(
                "SELECT fg.film_id, g.name AS genre_name " +
                        "FROM film_genre fg " +
                        "JOIN genre g ON fg.genre_id = g.id",
                rs -> {
                    Map<Integer, Set<String>> map = new HashMap<>();
                    while (rs.next()) {
                        int filmId = rs.getInt("film_id");
                        String genreName = rs.getString("genre_name");

                        Set<String> genres = map.get(filmId);
                        if (genres == null) {
                            genres = new HashSet<>();
                            map.put(filmId, genres);
                        }
                        genres.add(genreName);
                    }
                    return map;
                }
        );
    }
}
