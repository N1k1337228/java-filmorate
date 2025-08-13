package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;

import java.time.LocalDate;
import java.util.List;

public class FilmControllerTest {
    FilmController controller = new FilmController(new FilmService(new InMemoryFilmStorage()));
    Film film;
    ValidationException exception;

    @BeforeEach
    public void createFilm() {
        film = new Film();
        film.setId(1);
        film.setName("Film");
        film.setDescription("description");
        film.setDuration(120);
        film.setReleaseDate(LocalDate.of(2002, 2, 2));
    }

    @Test
    public void postRequestTest() {
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
        film.setDescription("a".repeat(201));
        exception = Assertions.assertThrows(ValidationException.class, () -> controller.createFilm(film));
        Assertions.assertEquals("максимальная длина описания — 200 символов", exception.getMessage());
    }

    @Test
    public void createIncorrectFilmsReleaseDateTest() {
        film.setReleaseDate(LocalDate.of(1800, 2, 2));
        exception = Assertions.assertThrows(ValidationException.class, () -> controller.createFilm(film));
        Assertions.assertEquals("дата релиза — не раньше 28 декабря 1895 года", exception.getMessage());
    }

    @Test
    public void createNegativeDurationOfFilm() {
        film.setDuration(-110);
        exception = Assertions.assertThrows(ValidationException.class, () -> controller.createFilm(film));
        Assertions.assertEquals("продолжительность фильма должна быть положительным числом", exception.getMessage());
    }

    @Test
    public void updateFilmEmptyIdTest() {
        film.setId(null);
        exception = Assertions.assertThrows(ValidationException.class, () -> controller.updateFilm(film));
        Assertions.assertEquals("Должен быть указан Id фильма", exception.getMessage());
    }

    @Test
    public void updateFilmEmptyDescriptionTest() {
        film.setDescription("");
        exception = Assertions.assertThrows(ValidationException.class, () -> controller.updateFilm(film));
        Assertions.assertEquals("Должно быть указано описание фильма", exception.getMessage());
    }

    @Test
    public void updateFilmTest() {
        controller.createFilm(film);
        film.setName("Интересный фильм");
        Assertions.assertEquals("Интересный фильм", controller.updateFilm(film).getName());
    }

    @Test
    public void getRequestUsers() {
        controller.createFilm(film);
        List<Film> filmList = controller.getAllFilms();
        Assertions.assertEquals(film, filmList.getFirst());
        System.out.println(film.getId());
    }
}
