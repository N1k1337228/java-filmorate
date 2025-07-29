package ru.yandex.practicum.filmorate;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDateTime;
import java.util.List;

public class UserControllerTest {

    UserController controller = new UserController();
    User user = new User();
    ValidationException exception;

    @Test
    public void postRequestTest() {
        user.setName("Gde");
        user.setEmail("Gde_email@");
        user.setBirthday(LocalDateTime.of(2000, 3, 4, 1, 3));
        user.setLogin("Gde228_1337");
        User user1 = controller.createNewUser(user);
        System.out.println(user1.getId());
        Assertions.assertNotNull(user1.getId());
    }

    @Test
    public void createUserEmptyEmail() {
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
    public void createUserEmptyLogin() {
        User user1 = new User();
        user1.setEmail("Gde228_email@");
        user1.setBirthday(LocalDateTime.of(2002, 3, 4, 1, 3));
        user1.setLogin("");
        exception = Assertions.assertThrows(ValidationException.class, () -> controller.createNewUser(user1));
        Assertions.assertEquals("логин не может быть пустым и содержать пробелы", exception.getMessage());
        User user2 = new User();
        user2.setEmail("Gde2283_email@");
        user2.setBirthday(LocalDateTime.of(2012, 3, 4, 1, 3));
        user2.setLogin("gde login");
        exception = Assertions.assertThrows(ValidationException.class, () -> controller.createNewUser(user2));
        Assertions.assertEquals("логин не может быть пустым и содержать пробелы", exception.getMessage());

    }

    @Test
    public void createUserIncorrectBirthday() {
        User user3 = new User();
        user3.setEmail("Gde2027_email@");
        user3.setBirthday(LocalDateTime.of(2027, 3, 4, 1, 3));
        user3.setLogin("gde566");
        user3.setName("Gde2027");
        exception = Assertions.assertThrows(ValidationException.class, () -> controller.createNewUser(user3));
        Assertions.assertEquals("Дата рождения не может быть в будущем", exception.getMessage());
    }

    @Test
    public void createUserEmptyNameTest() {
        User user4 = new User();
        user4.setEmail("Gde2000_email@");
        user4.setBirthday(LocalDateTime.of(2006, 3, 4, 1, 3));
        user4.setLogin("gde778");
        user4.setName("");
        Assertions.assertEquals("gde778", controller.createNewUser(user4).getName());
    }

    @Test
    public void updateUserIdNullTest() {
        User user5 = new User();
        user5.setEmail("Gde2009_email@");
        user5.setBirthday(LocalDateTime.of(2006, 3, 4, 1, 3));
        user5.setLogin("74545848");
        user5.setName("23313");
        exception = Assertions.assertThrows(ValidationException.class, () -> controller.updateUser(user5));
        Assertions.assertEquals("Должен быть указан Id пользователя", exception.getMessage());
    }

    @Test
    public void updateUserEmptyEmailTest() {
        User user6 = new User();
        user6.setId(7);
        user6.setBirthday(LocalDateTime.of(2006, 3, 4, 1, 3));
        user6.setLogin("745678");
        user6.setName("2543");
        exception = Assertions.assertThrows(ValidationException.class, () -> controller.updateUser(user6));
        Assertions.assertEquals("Должен быть указан email пользователя", exception.getMessage());
    }

    @Test
    public void updateUserEmptyBirthdayTest() {
        User user7 = new User();
        user7.setId(8);
        user7.setEmail("Gde2011_email@");
        user7.setLogin("745678");
        user7.setName("2543");
        exception = Assertions.assertThrows(ValidationException.class, () -> controller.updateUser(user7));
        Assertions.assertEquals("Должна быть указана дата рождения пользователя", exception.getMessage());
    }

    @Test
    public void updateUserEmptyNameTest() {
        User user8 = new User();
        user8.setId(9);
        user8.setEmail("Gde2011_email@");
        user8.setLogin("745678");
        user8.setBirthday(LocalDateTime.of(2015, 3, 4, 1, 3));
        exception = Assertions.assertThrows(ValidationException.class, () -> controller.updateUser(user8));
        Assertions.assertEquals("Должно быть указано имя пользователя", exception.getMessage());
    }

    @Test
    public void updateNotFoundUserTest() {
        User user9 = new User();
        user9.setId(10);
        user9.setName("Gde19999");
        user9.setEmail("Gde1999_email@");
        user9.setBirthday(LocalDateTime.of(1999, 3, 4, 1, 3));
        user9.setLogin("Gde228_1999");
        NotFoundException exception = Assertions.assertThrows(NotFoundException.class, () -> controller.updateUser(user9));
        Assertions.assertEquals("Пользователь не найден", exception.getMessage());
    }

    @Test
    public void updateUserTest() {
        User user1 = new User();
        user1.setEmail("Gde2011_email@");
        user1.setLogin("745678");
        user1.setEmail("Gde2011_email@");
        user1.setBirthday(LocalDateTime.of(2015, 3, 4, 1, 3));
        controller.createNewUser(user1);
        user1.setName("Gde19999");
        user1.setEmail("Gde1999_email@");
        user1.setBirthday(LocalDateTime.of(1999, 3, 4, 1, 3));
        Assertions.assertEquals("Gde19999", controller.updateUser(user1).getName());
        Assertions.assertEquals("Gde1999_email@", controller.updateUser(user1).getEmail());
        Assertions.assertEquals(LocalDateTime.of(1999, 3, 4, 1, 3),
                controller.updateUser(user1).getBirthday());
    }

    @Test
    public void getRequestUsers() {
        User user1 = new User();
        user1.setEmail("Gde2011_email@");
        user1.setLogin("745678");
        user1.setEmail("Gde2011_email@");
        user1.setBirthday(LocalDateTime.of(2015, 3, 4, 1, 3));
        controller.createNewUser(user1);
        List<User> userList = controller.getAllUsers();
        Assertions.assertEquals(user1, userList.getFirst());
    }
}
