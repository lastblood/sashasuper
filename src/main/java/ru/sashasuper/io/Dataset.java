package ru.sashasuper.io;


import ru.sashasuper.logic.Vector;

import java.util.AbstractMap.SimpleEntry;
import java.util.*;

public abstract class Dataset {
    public abstract List<SimpleEntry<Vector, Vector>> getAll();

    // Возвращает count подсписков. При любом count < getAll.size, не должно быть пустых
    public List<Dataset> getBatches(int count) {
        List<SimpleEntry<Vector, Vector>> all = new ArrayList<>(getAll());
        Collections.shuffle(all);
        List<Dataset> result = new ArrayList<>(count);

        int by = all.size() / count;
        for (int i = 0; i < count; i++)
            result.add(new SimpleDataset(all.subList(by * i, by * (i+1))));

        return result;
    }
}
