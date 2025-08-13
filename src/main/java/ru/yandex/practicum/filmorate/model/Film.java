package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class Film {
    private Integer id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer likes = 0;
    private List<Integer> usersIdLike = new ArrayList<>();
    private int duration;

    public void setUserOnLikeList(Integer userId) {
        if (usersIdLike == null) {
            usersIdLike = new ArrayList<>();
        }
        if (!usersIdLike.contains(userId)) {
            usersIdLike.add(userId);
            likes++;
        }
    }

    public void removeUserOnLikeList(Integer userId) {
        if (usersIdLike != null && usersIdLike.contains(userId)) {
            usersIdLike.remove(userId);
            likes--;
        }
    }
}
