package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.List;

public class FilmControllerTest {
    FilmController controller = new FilmController();
    Film film = new Film();
    ValidationException exception;

    @Test
    public void postRequestTest() {
        film.setName("Film");
        film.setDescription("description");
        film.setDuration(120);
        film.setReleaseDate(LocalDate.of(2002, 2, 2));
        Film film1 = controller.createFilm(film);
        Assertions.assertNotNull(film1.getId());
    }

    @Test
    public void createFilmEmptyName() {
        film.setName("");
        exception =
                Assertions.assertThrows(ValidationException.class, () -> controller.createFilm(film));
        Assertions.assertEquals("название не может быть пустым", exception.getMessage());
    }

    @Test
    public void createFilmsDescriptionLengthTest() {
        Film film1 = new Film();
        film1.setName("Film1");
        film1.setDescription("a".repeat(201));
        film1.setDuration(150);
        film1.setReleaseDate(LocalDate.of(2012, 2, 2));
        exception = Assertions.assertThrows(ValidationException.class, () -> controller.createFilm(film1));
        Assertions.assertEquals("максимальная длина описания — 200 символов", exception.getMessage());
    }

    @Test
    public void createIncorrectFilmsReleaseDateTest() {
        Film film1 = new Film();
        film1.setName("Film1");
        film1.setDescription("description1");
        film1.setDuration(150);
        film1.setReleaseDate(LocalDate.of(1800, 2, 2));
        exception = Assertions.assertThrows(ValidationException.class, () -> controller.createFilm(film1));
        Assertions.assertEquals("дата релиза — не раньше 28 декабря 1895 года", exception.getMessage());
    }

    @Test
    public void createNegativeDurationOfFilm() {
        Film film1 = new Film();
        film1.setName("Film123");
        film1.setDescription("description123");
        film1.setDuration(-110);
        film1.setReleaseDate(LocalDate.of(2015, 2, 2));
        exception = Assertions.assertThrows(ValidationException.class, () -> controller.createFilm(film1));
        Assertions.assertEquals("продолжительность фильма должна быть положительным числом", exception.getMessage());
    }

    @Test
    public void updateFilmEmptyIdTest() {
        Film film1 = new Film();
        film1.setName("Film123");
        film1.setDescription("description123");
        film1.setDuration(-110);
        film1.setReleaseDate(LocalDate.of(2015, 2, 2));
        exception = Assertions.assertThrows(ValidationException.class, () -> controller.updateFilm(film1));
        Assertions.assertEquals("Должен быть указан Id фильма", exception.getMessage());
    }

    @Test
    public void updateFilmEmptyDescriptionTest() {
        Film film1 = new Film();
        film1.setId(11);
        film1.setName("Film123");
        film1.setDuration(-110);
        film1.setReleaseDate(LocalDate.of(2015, 2, 2));
        exception = Assertions.assertThrows(ValidationException.class, () -> controller.updateFilm(film1));
        Assertions.assertEquals("Должно быть указано описание фильма", exception.getMessage());
    }

    @Test
    public void getRequestUsers() {
        Film film1 = new Film();
        film1.setName("Film123");
        film1.setDescription("description123");
        film1.setDuration(110);
        film1.setReleaseDate(LocalDate.of(2015, 2, 2));
        controller.createFilm(film1);
        List<Film> filmList = controller.getAllFilms();
        Assertions.assertEquals(film1, filmList.getFirst());
        System.out.println(film1.getId());
    }
}
