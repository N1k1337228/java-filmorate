package ru.yandex.practicum.filmorate.storage.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.mapper.UserMapper;

import java.util.*;


@Slf4j
@Repository
@Qualifier("UserDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbc;
    private final String addUserQuery = "INSERT INTO users (id, email, login, name, birthday) VALUES (?,?,?,?,?)";
    private final String updateUserQuery = "UPDATE users SET email=?, login=?, name=?, birthday=? WHERE id=?";
    private final String deleteUserQuery = "DELETE FROM users WHERE id=?";
    private final String allUsersQuery = "SELECT * FROM users";
    private final String findUserOnIdQuery = "SELECT * FROM users WHERE id=?";

    public User addUser(User user) {
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

    public Optional<User> getUserOnId(Integer id) {
        User user;
        try {
            user = jdbc.queryForObject(findUserOnIdQuery, new UserMapper(), id);

        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
        user.setFriends(new HashSet<Integer>(jdbc.queryForList("SELECT friend_id FROM users AS u INNER JOIN friendship AS f ON " +
                "f.user_id = u.id WHERE u.id = ?", Integer.class, id)));
        return Optional.of(user);
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
