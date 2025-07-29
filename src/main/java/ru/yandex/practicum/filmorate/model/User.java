package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(of = {"id", "email", "name", "login"})
public class User {
    private Integer id;
    @Email
    private String email;
    private String login;
    private String name;
    private LocalDateTime birthday;
}
