package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users/")
public class UserController {
    HashMap<Integer, User> userMap = new HashMap<>();

    @GetMapping
    public List<User> getAllUsers() {
        log.info("успешно обработан запрос:GET /users,возвращен список пользователей");
        return new ArrayList<>(userMap.values());
    }

    @PostMapping
    public User createNewUser(@Valid @RequestBody User user) {
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
        log.debug("успешно обработан запрос:POST /users, создан пользователь {}", user.getId());
        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
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
        if (userMap.containsKey(user.getId())) {
            User oldUser = userMap.get(user.getId());
            oldUser.setEmail(user.getEmail());
            oldUser.setBirthday(user.getBirthday());
            oldUser.setName(user.getName());
            log.info("успешно обработан запрос:PUT /users, успешно обновлён пользователь {}", user.getId());
            log.debug("успешно обработан запрос:PUT /users, успешно обновлён пользователь {}", user.getId());
            return oldUser;
        }
        log.error("Пользователь с Id {} не был найден", user.getId());
        throw new NotFoundException("Пользователь не найден");
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


