package ru.yandex.practicum.filmorate.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MPARatingStorage {
    private Map<Integer,String> ratings = new HashMap<>();

    public List<String> getAllRatings () {
        return new ArrayList<>(ratings.values());
    }
}
