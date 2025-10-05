package ru.yandex.practicum.filmorate.model;

import ru.yandex.practicum.filmorate.exceptions.ValidationException;

import java.util.Arrays;
import java.util.List;

public enum Mpa {
    G(1, "G"),
    PG(2, "PG"),
    PG_13(3, "PG-13"),
    R(4, "R"),
    NC_17(5, "NC-17");

    private final int id;
    private final String name;

    Mpa(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public static List<Mpa> getAll() {
        return Arrays.asList(values());
    }

    public static Mpa getById(int id) {
        for (Mpa mpa : values()) {
            if (mpa.id == id) {
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

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}

