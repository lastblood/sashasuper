package ru.sashasuper.io;

import ru.sashasuper.logic.Vector;

import java.util.ArrayList;
import java.util.List;

import static java.util.AbstractMap.SimpleEntry;

// Возвращает копию List с оригинальными векторами. Не трожь их, если хочешь жить
public class SimpleDataset extends Dataset {
    List<SimpleEntry<Vector, Vector>> list;

    public SimpleDataset(int initialCapacity) {
        this.list = new ArrayList<>(initialCapacity);
    }

    public SimpleDataset(List<SimpleEntry<Vector, Vector>> list) {
        this.list = list;
    }

    @Override
    public List<SimpleEntry<Vector, Vector>> getAll() {
        return list;
    }
}
