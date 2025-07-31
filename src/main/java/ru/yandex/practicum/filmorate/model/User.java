package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@EqualsAndHashCode(of = {"id", "email", "name", "login"})
public class User {
    private Integer id;
    @Email
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
}
