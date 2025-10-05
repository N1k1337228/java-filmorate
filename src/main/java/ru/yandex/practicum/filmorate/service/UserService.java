package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
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
        if (user.getFriends() == null || other.getFriends() == null ||
                user.getFriends().isEmpty() || other.getFriends().isEmpty()) {
            log.error("У пользователей пока нет друзей");
            throw new ValidationException("У пользователей пока нет друзей");
        }
        List<Integer> userFriends = new ArrayList<>(user.getFriends());
        List<Integer> otherFriends = new ArrayList<>(other.getFriends());
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

    public User getUserOnId(Integer id) {
        if (id == null) {
            log.error("Передан пустой id");
            throw new ValidationException("Пустой id пользователя");
        }
        return userStorage.getUserOnId(id);
    }

    public void addUserToFriends(Integer userId, Integer friendId) {
        if (userId == null || friendId == null) {
            log.error("передан пустой id");
            throw new ValidationException("Передан пустой id");
        }
        userStorage.addUserToFriends(userId, friendId);
    }

    public void removeFriend(Integer userId, Integer friendId) {
        if (userId == null || friendId == null) {
            log.error("Был передан пустой id");
            throw new ValidationException("передан пустой id");
        }
        userStorage.deleteUserFromFriend(userId, friendId);
    }

    public List<User> getUsersFriendList(Integer id) {
        if (id != null) {
            return userStorage.getListFriendsOnUsersId(id);
        }
        log.error("Был передан пустой id");
        throw new ValidationException("передан пустой id");
    }
}
