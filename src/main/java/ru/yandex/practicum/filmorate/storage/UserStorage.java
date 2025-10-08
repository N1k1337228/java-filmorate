package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    User addUser(User user);

    User updateUser(User user);

    User removeUser(User user);

    List<User> getAllUsers();

    Optional<User> getUserOnId(Integer id);

    List<User> getListFriendsOnUsersId(Integer userId);

    void deleteUserFromFriend(Integer userId, Integer friendId);

    void addUserToFriends(Integer userId, Integer friendId);

    List<User> getCommonFriends(Integer userId, Integer otherId);
}