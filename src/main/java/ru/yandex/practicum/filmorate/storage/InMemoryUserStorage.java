package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private final HashMap<Integer, User> userMap = new HashMap<>();

    public List<User> getAllUsers() {
        return new ArrayList<>(userMap.values());
    }

    public User addUser(User user) {
        if (user == null) {
            throw new ValidationException("пустое тело запроса");
        }
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
        user.setId(getNextId());
        userMap.put(user.getId(), user);
        log.info("успешно обработан запрос:POST /users, создан пользователь {}", user.getId());
        return user;
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
        if (userMap.containsKey(user.getId())) {
            userMap.put(user.getId(), user);
            log.info("успешно обработан запрос:PUT /users, успешно обновлён пользователь {}", user.getId());
            return user;
        }
        log.error("Пользователь с Id {} не был найден", user.getId());
        throw new NotFoundException("Пользователь не найден");
    }

    public User removeUser(User user) {
        if (user == null) {
            log.error("");
            throw new ValidationException("");
        }
        return userMap.remove(user.getId());
    }

    public User getUserOnId(Integer id) {
        return userMap.get(id);
    }

    private int getNextId() {
        int currentMaxId = userMap.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
