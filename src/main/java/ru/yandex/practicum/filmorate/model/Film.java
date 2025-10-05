package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class Film {
    private Integer id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Set<Integer> usersIdLike = new HashSet<>();
    private Integer likes = 0;
    private int duration;
    private List<Genre> genreOfFilm = new ArrayList<>();
    private Mpa raiting;

    public void setUserOnLikeList(Integer userId) {
        if (!usersIdLike.contains(userId)) {
            usersIdLike.add(userId);
            likes = usersIdLike.size();
        }
    }

    public void removeUserOnLikeList(Integer userId) {
        if (usersIdLike != null && usersIdLike.contains(userId)) {
            usersIdLike.remove(userId);
            likes--;
        }
    }
}
