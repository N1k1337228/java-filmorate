package ru.yandex.practicum.filmorate.storage.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mapper.GenreMapper;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class GenreRepository {
    private final JdbcTemplate jdbc;

    public List<Genre> getAllGenres() {
        List<Genre> genres = jdbc.query("SELECT * FROM genres", new GenreMapper());
        if (genres.isEmpty()) {
            throw new NotFoundException("Жанры не найдены");
        }
        return genres;
    }

    public Genre getGenreOnId(int id) {
        List<Genre> genre = jdbc.query("SELECT * FROM genres WHERE id = ?", new GenreMapper(), id);
        if (genre.isEmpty()) {
            throw new NotFoundException("Запрашиваемый жанр не найден");
        }
        return genre.get(0);
    }
}