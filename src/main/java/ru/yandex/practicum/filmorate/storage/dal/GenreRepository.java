package ru.yandex.practicum.filmorate.storage.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
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
        try {
            return jdbc.query("SELECT * FROM genre", new GenreMapper());
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Жанры не найдены");
        }
    }

    public Genre getGenreOnId(Integer id) {
        try {
            return jdbc.queryForObject("SELECT * FROM genre WHERE id = ?", new GenreMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Запрашиваемый жанр не найден");
        }
    }
}
