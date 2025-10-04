package ru.yandex.practicum.filmorate;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.dal.UserDbStorage;

import java.time.LocalDate;
import java.util.List;

public class UserControllerTest {

    UserController controller = new UserController(new UserService(new InMemoryUserStorage(),
            new UserDbStorage(new JdbcTemplate())));
    User user;
    ValidationException exception;

    @BeforeEach
    public void createUserTest() {
        user = new User();
        user.setId(1);
        user.setName("Gde");
        user.setEmail("Gde_email@");
        user.setBirthday(LocalDate.of(2000, 3, 4));
        user.setLogin("Gde228_1337");
    }

    @Test
    public void postRequestTest() {
        User user1 = controller.createNewUser(user);
        System.out.println(user1.getId());
        Assertions.assertNotNull(user1.getId());
    }

    @Test
    public void createUserEmptyEmailTest() {
        user.setEmail(null);
        exception =
                Assertions.assertThrows(ValidationException.class, () -> controller.createNewUser(user));
        Assertions.assertEquals("email не может быть пустым", exception.getMessage());
        user.setEmail("");
        exception = Assertions.assertThrows(ValidationException.class, () -> controller.createNewUser(user));
        Assertions.assertEquals("email не может быть пустым", exception.getMessage());
        user.setEmail("gde_email");
        exception = Assertions.assertThrows(ValidationException.class, () -> controller.createNewUser(user));
        Assertions.assertEquals("email не содержит символ: @", exception.getMessage());
    }

    @Test
    public void createUserEmptyLoginTest() {
        user.setLogin("");
        exception = Assertions.assertThrows(ValidationException.class, () -> controller.createNewUser(user));
        Assertions.assertEquals("логин не может быть пустым и содержать пробелы", exception.getMessage());
        user.setLogin("gde login");
        exception = Assertions.assertThrows(ValidationException.class, () -> controller.createNewUser(user));
        Assertions.assertEquals("логин не может быть пустым и содержать пробелы", exception.getMessage());
    }

    @Test
    public void createUserIncorrectBirthdayTest() {
        user.setBirthday(LocalDate.of(2027, 3, 4));
        exception = Assertions.assertThrows(ValidationException.class, () -> controller.createNewUser(user));
        Assertions.assertEquals("Дата рождения не может быть в будущем", exception.getMessage());
    }

    @Test
    public void createUserEmptyNameTest() {
        user.setName("");
        Assertions.assertEquals("Gde228_1337", controller.createNewUser(user).getName());
    }

    @Test
    public void updateUserIdNullTest() {
        user.setId(null);
        exception = Assertions.assertThrows(ValidationException.class, () -> controller.updateUser(user));
        Assertions.assertEquals("Должен быть указан Id пользователя", exception.getMessage());
    }

    @Test
    public void updateUserEmptyEmailTest() {
        user.setEmail("");
        exception = Assertions.assertThrows(ValidationException.class, () -> controller.updateUser(user));
        Assertions.assertEquals("Должен быть указан email пользователя", exception.getMessage());
    }

    @Test
    public void updateUserEmptyBirthdayTest() {
        user.setBirthday(null);
        exception = Assertions.assertThrows(ValidationException.class, () -> controller.updateUser(user));
        Assertions.assertEquals("Должна быть указана дата рождения пользователя", exception.getMessage());
    }

    @Test
    public void updateUserEmptyNameTest() {
        user.setName("");
        exception = Assertions.assertThrows(ValidationException.class, () -> controller.updateUser(user));
        Assertions.assertEquals("Должно быть указано имя пользователя", exception.getMessage());
    }

    @Test
    public void updateNotFoundUserTest() {
        user.setId(10);
        NotFoundException exception = Assertions.assertThrows(NotFoundException.class, () -> controller.updateUser(user));
        Assertions.assertEquals("Пользователь не найден", exception.getMessage());
    }

    @Test
    public void updateUserTest() {
        controller.createNewUser(user);
        user.setName("Gde19999");
        user.setEmail("Gde1999_email@");
        user.setBirthday(LocalDate.of(1999, 3, 4));
        Assertions.assertEquals("Gde19999", controller.updateUser(user).getName());
        Assertions.assertEquals("Gde1999_email@", controller.updateUser(user).getEmail());
        Assertions.assertEquals(LocalDate.of(1999, 3, 4),
                controller.updateUser(user).getBirthday());
    }

    @Test
    public void getRequestUsers() {
        controller.createNewUser(user);
        List<User> userList = controller.getAllUsers();
        Assertions.assertEquals(user, userList.getFirst());
    }
}
