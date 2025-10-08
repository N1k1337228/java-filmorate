package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("UserDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User createNewUser(User user) {
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            log.error("Email пользователя не был указан");
            throw new ValidationException("email не может быть пустым");
        }
        if (!(user.getEmail().contains("@"))) {
            log.error("Email пользователя не содержит @");
            throw new ValidationException("email не содержит символ: @");
        }
        if (user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.error("Пользователь не указал логин или он содержит пробелы");
            throw new ValidationException("логин не может быть пустым и содержать пробелы");
        }
        if (user.getBirthday().isAfter(LocalDate.now()) || user.getBirthday().isEqual(LocalDate.now())) {
            log.error("Дата рождения пользователя указана в будущем");
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            log.debug("Пользователь не указал имя: логин {} будет использоваться как имя", user.getLogin());
            user.setName(user.getLogin());
        }
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        if (user == null) {
            throw new ValidationException("пустое тело запроса");
        }
        if (user.getId() == null) {
            log.error("Пользователь не указал Id");
            throw new ValidationException("Должен быть указан Id пользователя");
        }
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            log.error("Пользователь не указал email");
            throw new ValidationException("Должен быть указан email пользователя");
        }
        if (user.getBirthday() == null) {
            log.error("Пользователь не указал дату рождения");
            throw new ValidationException("Должна быть указана дата рождения пользователя");
        }
        if (user.getName() == null || user.getName().isEmpty()) {
            log.error("Пользователь не указал имя");
            throw new ValidationException("Должно быть указано имя пользователя");
        }
        if (user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.error("пользователь не указал логин или он содержит пробелы");
            throw new ValidationException("логин не может быть пустым и содержать пробелы");
        }
        return userStorage.updateUser(user);
    }

    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public List<User> getMutualFriends(int userId, int otherId) {
        User user = userStorage.getUserOnId(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        User other = userStorage.getUserOnId(otherId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        if (user.getFriends() == null || other.getFriends() == null ||
                user.getFriends().isEmpty() || other.getFriends().isEmpty()) {
            log.error("У пользователей пока нет друзей");
            throw new ValidationException("У пользователей пока нет друзей");
        }
        return userStorage.getCommonFriends(userId, otherId);
    }

    public User getUserOnId(int id) {
        return userStorage.getUserOnId(id).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    public void addUserToFriends(int userId, int friendId) {
        if (!findUser(userId) || !findUser(friendId)) {
            log.error("Пользователь не найден");
            throw new NotFoundException("Пользователь не найден");
        }
        userStorage.addUserToFriends(userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        if (!findUser(userId) || !findUser(friendId)) {
            log.error("Пользователь не найден");
            throw new NotFoundException("Пользователь не найден");
        }
        userStorage.deleteUserFromFriend(userId, friendId);
    }

    public List<User> getUsersFriendList(int id) {
        if (!findUser(id)) {
            log.error("Пользователь не найден");
            throw new NotFoundException("Пользователь не найден");
        }
        return userStorage.getListFriendsOnUsersId(id);
    }

    private boolean findUser(int id) {
        return userStorage.getUserOnId(id).isPresent();
    }
}
