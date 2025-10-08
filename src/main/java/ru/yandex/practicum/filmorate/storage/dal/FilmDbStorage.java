package ru.yandex.practicum.filmorate.storage.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.storage.mapper.GenreMapper;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
@Qualifier("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbc;
    private final String insertNewFilm = "INSERT INTO films (title, description, release_date, likes_count, " +
            "duration, mpa_id) VALUES (?,?,?,?,?,?)";
    private final String updateFilm = "UPDATE films SET title=?, description=?, release_date=?, likes_count=?, " +
            "duration=?, mpa_id=? WHERE id = ?";
    private final String deleteFilm = "DELETE FROM films WHERE id = ?";
    private final String getAllFilm = """
                SELECT f.id,
                       f.title,
                       f.description,
                       f.release_date,
                       f.likes_count,
                       f.duration,
                       m.id AS mpa_id,
                       m.name AS mpa_name
                FROM films AS f
                JOIN mpa AS m ON f.mpa_id = m.id
            """;

    public Film addFilm(Film film) {
        if (!isMpaExists(film.getMpa().getId())) {
            log.error("Переданный рейтинг не найден");
            throw new NotFoundException("Переданный рейтинг не найден");
        }
        if (film.getGenres() != null) {
            if (!isGenreExists(film.getGenres())) {
                log.error("Жанр не найден");
                throw new NotFoundException("Жанр не найден");
            }
        }
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(insertNewFilm, new String[]{"id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getLikes());
            ps.setInt(5, film.getDuration());
            ps.setInt(6, film.getMpa().getId());
            return ps;
        }, keyHolder);
        film.setId(keyHolder.getKey().intValue());
        if (film.getGenres() != null) {
            Set<Genre> uniqueGenres = new TreeSet<>(Comparator.comparingInt(Genre::getId));
            uniqueGenres.addAll(film.getGenres());
            String sql = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
            List<Object[]> batchArgs = new ArrayList<>();
            Integer id = film.getId();
            for (Genre genre : uniqueGenres) {
                batchArgs.add(new Object[]{id, genre.getId()});
            }
            jdbc.batchUpdate(sql, batchArgs);
        }
        return film;
    }

    public Film updateFilm(Film film) {

        if (!isMpaExists(film.getMpa().getId())) {
            log.error("Переданный рейтинг не найден");
            throw new NotFoundException("Переданный рейтинг не найден");
        }
        if (film.getGenres() != null) {
            if (!isGenreExists(film.getGenres())) {
                log.error("Жанр не найден");
                throw new NotFoundException("Жанр не найден");
            }
        }
        Integer count = jdbc.update(updateFilm, film.getName(), film.getDescription(), film.getReleaseDate(), film.getLikes(),
                film.getDuration(), film.getMpa().getId(), film.getId());
        if (count == null || count == 0) {
            throw new NotFoundException("Фильм не найден");
        }
        if (film.getGenres() != null) {
            insertFilmsGenre(film.getGenres(), film.getId());
        }
        return film;
    }

    public void removeFilm(Integer filmId) {
        jdbc.update(deleteFilm, filmId);

    }

    public List<Film> getAllFilms() {
        List<Film> films = jdbc.query(getAllFilm, new FilmMapper());
        return getAllFilmsWithLikesAndGenres(films);
    }

    public Film getFilmOnId(Integer id) {
        String sql = getAllFilm + " WHERE f.id = ?";
        List<Film> films = jdbc.query(sql, new FilmMapper(), id);
        if (films.isEmpty()) {
            log.error("Фильм не найден");
            throw new NotFoundException("Фильм не найден");
        }
        return getAllFilmsWithLikesAndGenres(films).get(0);
    }

    public void addLike(Integer filmId, Integer userId) {
        try {
            jdbc.queryForObject("SELECT id FROM like_users WHERE film_id = ? AND user_id = ?",
                    Integer.class, filmId, userId);
        } catch (EmptyResultDataAccessException e) {
            jdbc.update("INSERT INTO like_users (film_id, user_id) VALUES(?,?)", filmId, userId);
            jdbc.update("UPDATE films SET likes_count = ? WHERE id = ?", getLikesOnFilm(filmId), filmId);

        }
    }

    public void removeLike(Integer filmId, Integer userId) {
        jdbc.update("DELETE FROM like_users WHERE film_id = ? AND user_id = ?", filmId, userId);
        jdbc.update("UPDATE films SET likes_count = ? WHERE id = ?", getLikesOnFilm(filmId), filmId);
    }

    public List<Film> getTheMostPopularFilms(Integer count) {
        String sql = getAllFilm + " ORDER BY f.likes_count DESC LIMIT ?";
        List<Film> films = jdbc.query(sql, new FilmMapper(), count);
        if (films.isEmpty()) {
            return new ArrayList<>();
        }
        return getAllFilmsWithLikesAndGenres(films);
    }

    public List<Film> getAllFilmsWithLikesAndGenres(List<Film> films) {
        if (films.isEmpty()) {
            return films;
        }
        List<Integer> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toList());
        Map<Integer, Set<Integer>> likesByFilm = getAllLikes();
        Map<Integer, List<Genre>> genresByFilm = getAllGenres(filmIds);
        for (Film film : films) {
            film.setUsersIdLike(likesByFilm.getOrDefault(film.getId(), Set.of()));
            film.setGenres(genresByFilm.getOrDefault(film.getId(), List.of()));
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

    private Map<Integer, List<Genre>> getAllGenres(List<Integer> filmIds) {
        if (filmIds == null || filmIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String inSql = filmIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        String sql = "SELECT fg.film_id, g.id AS genre_id, g.name AS genre_name " +
                "FROM film_genre fg " +
                "JOIN genres g ON fg.genre_id = g.id " +
                "WHERE fg.film_id IN (" + inSql + ")";

        return jdbc.query(sql, rs -> {
            Map<Integer, List<Genre>> map = new HashMap<>();
            while (rs.next()) {
                int filmId = rs.getInt("film_id");
                int genreId = rs.getInt("genre_id");
                String genreName = rs.getString("genre_name");

                Genre genre = new Genre();
                genre.setId(genreId);
                genre.setName(genreName);

                map.computeIfAbsent(filmId, k -> new ArrayList<>()).add(genre);
            }
            return map;
        });
    }


    private boolean isMpaExists(int mpaId) {
        String sql = "SELECT COUNT(*) FROM mpa WHERE id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, mpaId);
        return count != null && count > 0;
    }

    private boolean isGenreExists(List<Genre> genres) {
        String sql = "SELECT * FROM genres";
        Set<Genre> dbGenres = new HashSet<>(jdbc.query(sql, new GenreMapper()));
        return dbGenres.containsAll(genres);
    }

    private void insertFilmsGenre(List<Genre> genres, int filmId) {
        Set<Genre> uniqueGenres = new TreeSet<>(Comparator.comparingInt(Genre::getId));
        uniqueGenres.addAll(genres);
        String sql = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
        List<Object[]> batchArgs = new ArrayList<>();
        int id = filmId;
        for (Genre genre : uniqueGenres) {
            batchArgs.add(new Object[]{id, genre.getId()});
        }
        jdbc.batchUpdate(sql, batchArgs);
    }

    private Integer getLikesOnFilm(Integer filmId) {
        return jdbc.queryForObject(
                "SELECT COUNT(*) FROM like_users WHERE film_id = ?",
                Integer.class, filmId);
    }
}