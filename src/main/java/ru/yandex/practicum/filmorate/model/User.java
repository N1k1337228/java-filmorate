package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class User {
    private Integer id;
    @Email
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
    private Set<Integer> friends;

    public void setFriends(Integer friend) {
        friends.add(friend);
    }

    public void removeOnFriend(Integer friend) {
        friends.remove(friend);
    }


}
