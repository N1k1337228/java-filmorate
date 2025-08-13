package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class Film {
    private Integer id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer likes;
    private List<Integer> usersIdLike;
    private int duration;

    private void setLike() {
        likes++;
    }

    private void removeLike() {
        likes--;
    }

    public void setUserOnLikeList(Integer userId) {
        if (usersIdLike.contains(userId)) {
            return;
        }
        usersIdLike.add(userId);
        setLike();
    }

    public void removeUserOnLikeList(Integer userId) {
        if (!usersIdLike.contains(userId)) {
            return;
        }
        usersIdLike.remove(userId);
        removeLike();
    }
}
