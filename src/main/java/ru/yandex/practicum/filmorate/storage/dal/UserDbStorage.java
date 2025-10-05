package ru.yandex.practicum.filmorate.storage.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.mapper.UserMapper;

import java.time.LocalDate;
import java.util.*;


@Slf4j
@Repository
@Qualifier("UserDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbc;
    private final String addUserQuery = "INSERT INTO users VALUES (?,?,?,?,?)";
    private final String updateUserQuery = "UPDATE users SET email=?, login=?, name=?, birthday=? WHERE id=?";
    private final String deleteUserQuery = "DELETE FROM users WHERE id=?";
    private final String allUsersQuery = "SELECT * FROM users";
    private final String findUserOnIdQuery = "SELECT * FROM users WHERE id=?";

    public User addUser(User user) {
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
        if (user.getId() == null) {
            Integer nextId = jdbc.queryForObject(
                    "SELECT COALESCE(MAX(id), 0) + 1 FROM users",
                    Integer.class
            );
            user.setId(nextId);
        }
        try {
            jdbc.queryForObject("SELECT id FROM users WHERE id=?", Integer.class, user.getId());

        } catch (EmptyResultDataAccessException e) {
            jdbc.update(addUserQuery, user.getId(), user.getEmail(), user.getLogin(), user.getName(), user.getBirthday());
        }
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
        int count = jdbc.update(updateUserQuery, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
        if (count > 0) {
            return user;
        }
        throw new NotFoundException("Пользователь не найден");
    }

    public User removeUser(User user) {
        int count = jdbc.update(deleteUserQuery, user.getId());
        if (count > 0) {
            return user;
        }
        throw new NotFoundException("Пользователь не найден");
    }

    public List<User> getAllUsers() {
        List<User> users = jdbc.query(allUsersQuery, new UserMapper());
        if (users.isEmpty()) {
            throw new NotFoundException("Пользователь не найден");
        }
        return getAllUsersWithFriends(users);
    }

    public User getUserOnId(Integer id) {
        User user = jdbc.queryForObject(findUserOnIdQuery, new UserMapper(), id);
        user.setFriends(new HashSet<Integer>(jdbc.queryForList("SELECT friend_id FROM users AS u INNER JOIN friendship AS f ON " +
                "f.user_id = u.id WHERE u.id = ?", Integer.class, id)));
        return user;
    }

    public void addUserToFriends(Integer userId, Integer friendId) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM users " +
                "WHERE id IN (?,?)", Integer.class, userId, friendId);
        if (count == null || count != 2) {
            throw new NotFoundException("Нет пользователей для добавления в друзья");
        }
        Integer countUserToFriend = jdbc.queryForObject("SELECT COUNT(*) FROM " +
                "friendship WHERE user_id = ? AND friend_id = ?", Integer.class, userId, friendId);
        if (countUserToFriend == null || countUserToFriend == 0) {
            jdbc.update("INSERT INTO friendship (user_id,friend_id) VALUES(?,?)", userId, friendId);
        }
        Integer countFriendToUser = jdbc.queryForObject("SELECT COUNT(*) FROM users AS u INNER JOIN friendship AS ul ON u.id = ul.user_id " +
                "WHERE user_id = ? AND friend_id = ?", Integer.class, friendId, userId);
        if (countFriendToUser != null && countFriendToUser > 0) {
            jdbc.update("UPDATE friendship SET status = 'CONFIRMED' " +
                    "WHERE user_id = ? AND friend_id = ?", userId, friendId);
            jdbc.update("UPDATE friendship SET status = 'CONFIRMED' " +
                    "WHERE user_id = ? AND friend_id = ?", friendId, userId);
        }
    }

    public void deleteUserFromFriend(Integer userId, Integer friendId) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM users " +
                "WHERE id IN (?,?)", Integer.class, userId, friendId);
        if (count == null || count != 2) {
            throw new NotFoundException("Нет пользователей для добавления в друзья");
        }
        Integer countUserToFriend = jdbc.queryForObject("SELECT COUNT(*) FROM " +
                "friendship WHERE user_id = ? AND friend_id = ?", Integer.class, userId, friendId);
        if (countUserToFriend != null && countUserToFriend > 0) {
            jdbc.update("DELETE FROM friendship WHERE user_id = ? AND friend_id = ?", userId, friendId);
        }
        Integer countFriendToUser = jdbc.queryForObject("SELECT COUNT(*) FROM friendship " +
                "WHERE user_id = ? AND friend_id = ?", Integer.class, friendId, userId);
        if (countFriendToUser != null && countFriendToUser > 0) {
            jdbc.update("UPDATE friendship SET status = 'PENDING' " +
                    "WHERE user_id = ? AND friend_id = ?", friendId, userId);
        }
    }

    public List<User> getListFriendsOnUsersId(Integer userId) {
        Integer countOfUsers = jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE id=?", Integer.class, userId);
        if (countOfUsers == null || countOfUsers == 0) {
            throw new NotFoundException("Пользователь не найден");
        }
        List<User> friends = jdbc.query("SELECT u.* FROM friendship AS fs " +
                "INNER JOIN users AS u ON u.id = fs.friend_id WHERE user_id = ?", new UserMapper(), userId);
        if (friends.isEmpty()) {
            return new ArrayList<User>();
        }
        return getAllUsersWithFriends(friends);
    }

    public List<User> getCommonFriends(Integer userId, Integer otherUserId) {
        // SQL находит общих друзей по userId и otherUserId и сразу получает их данные
        String sql = "SELECT u.* " +
                "FROM users u " +
                "INNER JOIN friendship f1 ON u.id = f1.friend_id " +
                "INNER JOIN friendship f2 ON u.id = f2.friend_id " +
                "WHERE f1.user_id = ? AND f2.user_id = ?";
        List<User> commonFriends = jdbc.query(sql, new UserMapper(), userId, otherUserId);
        if (!commonFriends.isEmpty()) {
            return getAllUsersWithFriends(commonFriends);
        }
        return commonFriends;
    }

    private List<User> getAllUsersWithFriends(List<User> users) {
        if (users.isEmpty()) {
            return users;
        }
        Map<Integer, Set<Integer>> friendsByUser = jdbc.query(
                "SELECT user_id, friend_id FROM friendship",
                rs -> {
                    Map<Integer, Set<Integer>> map = new HashMap<>();
                    while (rs.next()) {
                        int userId = rs.getInt("user_id");
                        int friendId = rs.getInt("friend_id");

                        Set<Integer> friends = map.get(userId);
                        if (friends == null) {
                            friends = new HashSet<>();
                            map.put(userId, friends);
                        }
                        friends.add(friendId);
                    }
                    return map;
                }
        );
        for (User user : users) {
            user.setFriends(friendsByUser.getOrDefault(user.getId(), Set.of()));
        }
        return users;
    }
}
