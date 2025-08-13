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
            log.error("у пользователей пока нет друзей");
            throw new ValidationException("У пользователей пока нет друзей");
        }
        ArrayList<Integer> firstListFriends = new ArrayList<>(user.getFriends());
        ArrayList<Integer> secondListFriends = new ArrayList<>(other.getFriends());
        ArrayList<User> mutualFriends = new ArrayList<>();
        if (firstListFriends.isEmpty() ||
                secondListFriends.isEmpty()) {
            log.error("");
            throw new ValidationException("");
        }
        ArrayList<Integer> smaller;
        ArrayList<Integer> large;
        if (firstListFriends.size() >= secondListFriends.size()) {
            smaller = secondListFriends;
            large = firstListFriends;
        } else {
            smaller = firstListFriends;
            large = secondListFriends;
        }
        for (Integer id : smaller) {
            if (large.contains(id)) {
                mutualFriends.add(userStorage.getUserOnId(id));
            }
        }
        return mutualFriends;
    }

    // хотел спросить лучше использовать мой вариант написанный выше или воспользоваться методом retainAll ?
    // возможно лучше такие вопросы задавать наставнику, но иногда он долго отвечает.

    public User addUserToFriends(Integer userId, Integer friendId) {
        if (userId == null || friendId == null) {
            log.error("передан пустой id");
            throw new ValidationException("Передан пустой id");
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
            return user;
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
            return friend;
        }
        throw new ValidationException("Пользователи не являются друзьями и не могут быть удалены из списка друзей");
    }

    public List<User> getUsersFriendList(Integer id) {
        User user = userStorage.getUserOnId(id);
        HashSet<User> friends = new HashSet<>();
        if (user == null) {
            log.error("");
            throw new ValidationException("");
        }
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
