package ru.sashasuper.io;


import ru.sashasuper.logic.Vector;

import java.util.AbstractMap.SimpleEntry;
import java.util.*;

// Допытливый зритель, конечно, спросит: "Ну и нафига он нужен абстрактным"?
// Отвечаю: для возможной ленивой инициализации
public abstract class Dataset {
    public abstract List<SimpleEntry<Vector, Vector>> getAll();

    // Возвращает count подсписков. Точный результат зависит от реализации,
    // единственный контракт, что при любом count < getAll.size, они должны быть не пустыми
    public List<Dataset> getBatches(int count) {
        List<SimpleEntry<Vector, Vector>> all = getAll();
        List<Dataset> result = new ArrayList<>(count);

        int by = all.size() / count;
        for (int i = 0; i < count; i++)
            result.add(new SimpleDataset(all.subList(by * i, by * (i+1))));

        return result;
    }
}
