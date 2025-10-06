package ru.yandex.practicum.filmorate.storage.mapper;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FilmMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getInt("id"));
        film.setName(rs.getString("title"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setLikes(rs.getInt("likes_count"));
        film.setDuration(rs.getInt("duration"));
        String rating = rs.getString("rating");
        film.setMpa(createMpaFromRating(rating));
        return film;
    }

    private Mpa createMpaFromRating(String rating) {
        switch (rating) {
            case "G":
                return new Mpa(1, "G");
            case "PG":
                return new Mpa(2, "PG");
            case "PG-13":
                return new Mpa(3, "PG-13");
            case "R":
                return new Mpa(4, "R");
            case "NC-17":
                return new Mpa(5, "NC-17");
            default:
                throw new IllegalArgumentException("Unknown rating: " + rating);
        }
    }

}



