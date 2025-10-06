package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;

import java.util.Arrays;
import java.util.List;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Mpa {
    G(1, "G"),
    PG(2, "PG"),
    PG_13(3, "PG-13"),
    R(4, "R"),
    NC_17(5, "NC-17");

    private final Integer id;
    private final String name;

    Mpa(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public static List<Mpa> getAll() {
        return Arrays.asList(values());
    }

    public static Mpa getById(Integer id) {
        for (Mpa mpa : values()) {
            if (mpa.id.equals(id)) {
                return mpa;
            }
        }
        throw new ValidationException("MPA с id=" + id + " не найден");
    }

    public static Mpa fromString(String name) {
        for (Mpa mpa : values()) {
            if (mpa.name.equals(name)) {
                return mpa;
            }
        }
        throw new IllegalArgumentException("No enum constant for: " + name);
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}

