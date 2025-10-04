package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.dal.GenreRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GenreService {
    private static JdbcTemplate jdbc;
    private static GenreRepository genreRepository;

    public List<Genre> getAllGenres() {
        return genreRepository.getAllGenres();
    }

    public Genre getGenreOnId(Integer id) {
        if (id != null) {
            return genreRepository.getGenreOnId(id);
        }
        log.error("Передан пустой id жанра");
        throw new ValidationException("Нужно передать id жанра!");
    }
}
