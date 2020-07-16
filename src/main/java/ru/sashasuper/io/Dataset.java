package ru.sashasuper.io;


import ru.sashasuper.logic.Vector;

import java.util.AbstractMap.SimpleEntry;
import java.util.*;

// Допытливый зритель, конечно, спросит: "Ну и нафига он нужен абстрактным"?
// Отвечаю: для возможной ленивой инициализации
public abstract class Dataset {
    public abstract List<SimpleEntry<ru.sashasuper.logic.Vector, ru.sashasuper.logic.Vector>> getAll();

    // Возвращает count подсписков. Точный результат зависит от реализации,
    // единственный контракт, что при любом count < getAll.size, они должны быть не пустыми
    public List<List<SimpleEntry<ru.sashasuper.logic.Vector, ru.sashasuper.logic.Vector>>> getBatches(int count) {
        List<List<SimpleEntry<ru.sashasuper.logic.Vector, ru.sashasuper.logic.Vector>>> list = new ArrayList<>();
        List<SimpleEntry<ru.sashasuper.logic.Vector, Vector>> all = getAll();

        int by = all.size() / count;
        for (int i = 0; i < count; i++)
            list.add(all.subList(by * i, by * (i+1)));

        return Collections.unmodifiableList(list);
//        return list;
    }
}
