package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mapper.MpaMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MpaService {

    private final JdbcTemplate jdbc;

    public List<Mpa> getAllMpa() {
        return jdbc.query("SELECT * FROM mpa", new MpaMapper());
    }

    public Mpa getMpaById(int id) {
        List<Mpa> mpa = jdbc.query("SELECT * FROM mpa WHERE id = ?", new MpaMapper(),id);
        if (mpa.isEmpty()) {
            throw new NotFoundException("Запрашиваемый mpa не найден");
        }
        return mpa.getFirst();
    }
}
