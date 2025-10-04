package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping
    public List<User> getAllUsers() {
        log.info("успешно обработан запрос:GET /users,возвращен список пользователей");
        return userService.getAllUsers();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public User createNewUser(@Valid @RequestBody User user) {
        return userService.createNewUser(user);

    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        return userService.updateUser(user);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addUserToFriends(@PathVariable("id") Integer userId, @PathVariable Integer friendId) {
        userService.addUserToFriends(userId, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFromFriendsList(@PathVariable("id") Integer userId, @PathVariable Integer friendId) {
        userService.removeFriend(userId, friendId);

    }

    @GetMapping("/{id}/friends")
    public List<User> getUsersFriendList(@PathVariable("id") Integer userId) {
        return userService.getUsersFriendList(userId);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getMutualFriends(@PathVariable("id") Integer userId, @PathVariable Integer otherId) {
        return userService.getMutualFriends(userId, otherId);
    }
}