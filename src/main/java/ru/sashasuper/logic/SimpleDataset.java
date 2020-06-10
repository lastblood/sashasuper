package ru.sashasuper.logic;

import java.util.ArrayList;
import java.util.List;

import static java.util.AbstractMap.SimpleEntry;

// Возвращает копию List с оригинальными векторами. Не трожь их, если хочешь жить
public class SimpleDataset extends Dataset {
    ArrayList<SimpleEntry<Vector, Vector>> list;

    public SimpleDataset() {
    }

    public SimpleDataset(int initialCapacity) {
        this.list = new ArrayList<>(initialCapacity);
    }

    public SimpleDataset(ArrayList<SimpleEntry<Vector, Vector>> list) {
        this.list = list;
    }

    @Override
    public List<SimpleEntry<Vector, Vector>> getAll() {
        return (List<SimpleEntry<Vector, Vector>>) list.clone();
    }
}
