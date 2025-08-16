package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User createNewUser(User user) {
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public List<User> getMutualFriends(Integer userId, Integer otherId) {
        if (userId == null || otherId == null) {
            log.error("Передан пустой id");
            throw new ValidationException("Передан пустой id");
        }
        User user = userStorage.getUserOnId(userId);
        User other = userStorage.getUserOnId(otherId);
        if (user == null || other == null) {
            log.error("Пользователь не найден");
            throw new NotFoundException("Пользователь не найден");
        }
        if (user.getFriends() == null || other.getFriends() == null) {
            log.error("У пользователей пока нет друзей");
            throw new ValidationException("У пользователей пока нет друзей");
        }
        List<Integer> userFriends = new ArrayList<>(user.getFriends());
        List<Integer> otherFriends = new ArrayList<>(other.getFriends());
        if (userFriends.isEmpty() || otherFriends.isEmpty()) {
            log.error("У пользователей пока нет друзей");
            throw new ValidationException("У пользователей пока нет друзей");
        }
        userFriends.retainAll(otherFriends);
        List<User> mutualFriends = new ArrayList<>();
        for (Integer friendId : userFriends) {
            User friend = userStorage.getUserOnId(friendId);
            if (friend != null) {
                mutualFriends.add(friend);
            }
        }
        return mutualFriends;
    }

    public User addUserToFriends(Integer userId, Integer friendId) {
        if (userId == null || friendId == null) {
            log.error("передан пустой id");
            throw new ValidationException("Передан пустой id");
        }
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить себя в друзья");
        }
        User user = userStorage.getUserOnId(userId);
        User friend = userStorage.getUserOnId(friendId);
        if (user == null || friend == null) {
            log.error("пользователь не найден");
            throw new NotFoundException("Пользователь не найден");
        }
        if (!isFriends(userId, friendId)) {
            user.setFriends(friendId);
            friend.setFriends(userId);
            userStorage.updateUser(friend);
            return userStorage.updateUser(user);
        }
        throw new ValidationException("Пользователи уже друзья");
    }

    public User removeFriend(Integer userId, Integer friendId) {
        if (userId == null || friendId == null) {
            log.error("Был передан пустой id");
            throw new ValidationException("передан пустой id");
        }
        User user = userStorage.getUserOnId(userId);
        User friend = userStorage.getUserOnId(friendId);
        if (user == null || friend == null) {
            log.error("пользователь не был найден");
            throw new NotFoundException("Пользователь не был найден");
        }
        if (isFriends(userId, friendId)) {
            friend.removeOnFriend(userId);
            user.removeOnFriend(friendId);
            userStorage.updateUser(friend);
            userStorage.updateUser(user);
        }
        return user;
    }

    public List<User> getUsersFriendList(Integer id) {
        User user = userStorage.getUserOnId(id);
        if (user == null) {
            log.error("Пользователь не был найден");
            throw new NotFoundException("пользователь не был найден");
        }
        HashSet<User> friends = new HashSet<>();
        for (Integer userId : user.getFriends()) {
            if (userStorage.getAllUsers().contains(userStorage.getUserOnId(userId))) {
                friends.add(userStorage.getUserOnId(userId));
            }
        }
        return new ArrayList<>(friends);
    }

    private boolean isFriends(Integer userId, Integer friendId) {
        return userStorage.getUserOnId(userId).getFriends().contains(friendId) &&
                userStorage.getUserOnId(friendId).getFriends().contains(userId);
    }
}
