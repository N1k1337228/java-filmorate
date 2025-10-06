package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dal.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.dal.GenreRepository;
import ru.yandex.practicum.filmorate.storage.dal.UserDbStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, FilmDbStorage.class, GenreRepository.class})
class FilmorateApplicationTests {

    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;
    private final GenreRepository genreRepository;
    private final JdbcTemplate jdbc;
    User user1;
    User user2;
    Film film1;
    Film film2;

    @BeforeEach
    public void createUserTest() {
        user1 = new User();
        user1.setId(1);
        user1.setName("Gde1");
        user1.setEmail("Gde1_email@");
        user1.setBirthday(LocalDate.of(2001, 1, 1));
        user1.setLogin("Gde228_1337_1");
        user2 = new User();
        user2.setId(2);
        user2.setName("Gde2");
        user2.setEmail("Gde2_email@");
        user2.setBirthday(LocalDate.of(2002, 2, 2));
        user2.setLogin("Gde228_1337_2");
    }

    @BeforeEach
    public void createFilm() {
        film1 = new Film();
        film1.setId(1);
        film1.setName("Film1");
        film1.setDescription("description1");
        film1.setDuration(121);
        film1.setReleaseDate(LocalDate.of(2001, 1, 1));
        film1.setMpa(new Mpa(1, "G"));

        film2 = new Film();
        film2.setId(2);
        film2.setName("Film2");
        film2.setDescription("description2");
        film2.setDuration(122);
        film2.setReleaseDate(LocalDate.of(2002, 2, 2));
        film2.setMpa(new Mpa(2, "G"));

    }

    @Test
    public void testFindUserById() {
        User user = userStorage.getUserOnId(1);
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1);
    }

    @Test
    public void testGetAllUsers() {
        List<User> users = userStorage.getAllUsers();
        Assertions.assertFalse(users.isEmpty());
    }

    @Test
    public void testAddUserToFriends() {
        userStorage.addUserToFriends(1, 2);
        Integer id = jdbc.queryForObject("SELECT id FROM friendship " +
                "WHERE user_id = ? AND friend_id = ?", Integer.class, 1, 2);
        Assertions.assertNotNull(id);
    }

    @Test
    public void testRemoveUserFromFriends() {
        userStorage.deleteUserFromFriend(1, 2);
        Assertions.assertThrows(EmptyResultDataAccessException.class, () ->
                jdbc.queryForObject(
                        "SELECT id FROM friendship WHERE user_id = ? AND friend_id = ?",
                        Integer.class,
                        1, 2
                )
        );
    }

    @Test
    public void testAddUser() {
        User user3 = new User();
        user3.setName("Gde3");
        user3.setEmail("Gde3_email@");
        user3.setBirthday(LocalDate.of(2003, 2, 2));
        user3.setLogin("Gde228_1337_3");
        userStorage.addUser(user3);
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM users " +
                "WHERE id = ?", Integer.class, 3);
        Assertions.assertNotNull(count);
        Assertions.assertEquals(1, count);
    }

    @Test
    public void testUpdateUser() {
        User newUser1 = new User();
        newUser1 = new User();
        newUser1.setId(1);
        newUser1.setName("newGde1");
        newUser1.setEmail("newGde1_email@");
        newUser1.setBirthday(LocalDate.of(2001, 1, 11));
        newUser1.setLogin("newGde228_1337_1");
        userStorage.updateUser(newUser1);
        List<String> newUsersParam = jdbc.queryForList("SELECT name FROM users " +
                "WHERE id = ?", String.class, 1);
        Assertions.assertNotNull(newUsersParam);
        Assertions.assertEquals("newGde1", newUsersParam.getFirst());
    }

    @Test
    public void testDeleteUser() {
        userStorage.removeUser(user1);
        Assertions.assertThrows(EmptyResultDataAccessException.class, () ->
                jdbc.queryForObject(
                        "SELECT id FROM users WHERE id = ?",
                        Integer.class,
                        1
                )
        );
    }

    @Test
    public void testGetFriendListOnUserId() {
        userStorage.addUserToFriends(1, 2);
        List<User> friends = userStorage.getListFriendsOnUsersId(1);
        Assertions.assertFalse(friends.isEmpty());
        Assertions.assertEquals(2, friends.getFirst().getId());
    }

    @Test
    public void testGetCommonFriends() {
        User user3 = new User();
        user3.setId(3);
        user3.setName("Gde3");
        user3.setEmail("Gde3_email@");
        user3.setBirthday(LocalDate.of(2003, 2, 2));
        user3.setLogin("Gde228_1337_3");
        userStorage.addUser(user3);
        userStorage.addUserToFriends(1, 2);
        userStorage.addUserToFriends(3, 2);
        List<User> friends = userStorage.getListFriendsOnUsersId(3);
        Assertions.assertFalse(friends.isEmpty());
        Assertions.assertEquals(2, friends.getFirst().getId());
        List<User> friend = userStorage.getCommonFriends(1, 3);
        Assertions.assertFalse(friend.isEmpty());
        Assertions.assertEquals(2, friend.get(0).getId());
    }

    @Test
    public void testAddFilm() {
        Film film3 = new Film();
        film3.setName("Film3");
        film3.setDescription("description3");
        film3.setDuration(123);
        film3.setReleaseDate(LocalDate.of(2003, 3, 3));
        film3.setMpa(new Mpa(1, "G"));
        filmStorage.addFilm(film3);
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM films " +
                "WHERE id = ?", Integer.class, 3);
        Assertions.assertNotNull(count);
        Assertions.assertEquals(1, count);
    }

    @Test
    public void testUpdateFilm() {
        Film newFilm1 = new Film();
        newFilm1.setId(1);
        newFilm1.setName("newFilm1");
        newFilm1.setDescription("newDescription1");
        newFilm1.setDuration(132);
        newFilm1.setReleaseDate(LocalDate.of(2003, 4, 3));
        newFilm1.setMpa(new Mpa(4, "R"));
        filmStorage.updateFilm(newFilm1);
        List<String> newUsersParam = jdbc.queryForList("SELECT title FROM films " +
                "WHERE id = ?", String.class, 1);
        Assertions.assertNotNull(newUsersParam);
        Assertions.assertEquals("newFilm1", newUsersParam.getFirst());
    }

    @Test
    public void testDeleteFilm() {
        filmStorage.removeFilm(film1.getId());
        Assertions.assertThrows(EmptyResultDataAccessException.class, () ->
                jdbc.queryForObject(
                        "SELECT id FROM films WHERE id = ?",
                        Integer.class,
                        1
                )
        );
    }

    @Test
    public void testFindFilmById() {
        Film film = filmStorage.getFilmOnId(1);
        assertThat(film).isNotNull();
        assertThat(film.getId()).isEqualTo(1);
    }

    @Test
    public void testAddLike() {
        filmStorage.addLike(film1.getId(), user1.getId());
        Integer id = jdbc.queryForObject("SELECT id FROM like_users " +
                "WHERE user_id = ? AND film_id = ?", Integer.class, user1.getId(), film1.getId());
        Assertions.assertNotNull(id);
        Integer count = jdbc.queryForObject("SELECT likes_count FROM films " +
                "WHERE id = ?", Integer.class, film1.getId());
        Assertions.assertEquals(film1.getLikes() + 1, count);
    }

    @Test
    public void testRemoveLike() {
        filmStorage.addLike(film2.getId(), user2.getId());
        filmStorage.removeLike(film2.getId(), user2.getId());
        Integer count = jdbc.queryForObject("SELECT likes_count FROM films " +
                "WHERE id = ?", Integer.class, film2.getId());
        Assertions.assertEquals(0, count);
        Assertions.assertThrows(EmptyResultDataAccessException.class, () -> jdbc.queryForObject("SELECT id " +
                "FROM like_users WHERE user_id = ? AND film_id = ?", Integer.class, user2.getId(), film2.getId()));
    }

    @Test
    public void testGetAllFilms() {
        List<Film> films = filmStorage.getAllFilms();
        Assertions.assertNotNull(films);
        Assertions.assertFalse(films.isEmpty());
        Assertions.assertEquals(2, films.size());
    }

    @Test
    public void testGetTheMostPopularFilms() {
        filmStorage.addLike(film1.getId(), user1.getId());
        filmStorage.addLike(film1.getId(), user2.getId());
        filmStorage.addLike(film2.getId(), user1.getId());
        List<Film> films = filmStorage.getTheMostPopularFilms(2);
        Assertions.assertFalse(films.isEmpty());
        Assertions.assertEquals(2, films.size());
        Assertions.assertEquals(2, films.getFirst().getLikes());
    }

    @Test
    public void testGetAllGenre() {
        List<Genre> genres = genreRepository.getAllGenres();
        Assertions.assertFalse(genres.isEmpty());
        Assertions.assertEquals(6, genres.size());
    }

    @Test
    public void testGenreOnId() {
        Genre genre = genreRepository.getGenreOnId(1);
        Assertions.assertNotNull(genre);
        Assertions.assertEquals("Комедия", genre.getName());
    }

    @Test
    public void testAddGenreInFilm() {
        Genre genre = genreRepository.getGenreOnId(1);
        ArrayList<Genre> genres = new ArrayList<>();
        genres.add(genre);
        Film newFilm2 = new Film();
        newFilm2.setName("newFilm1");
        newFilm2.setDescription("newDescription1");
        newFilm2.setDuration(132);
        newFilm2.setReleaseDate(LocalDate.of(2003, 4, 3));
        newFilm2.setMpa(new Mpa(4, "R"));
        newFilm2.setGenres(genres);
        filmStorage.addFilm(newFilm2);
        Assertions.assertEquals(genre, filmStorage.getFilmOnId(3).getGenres().get(0));
    }
}
