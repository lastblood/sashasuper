package ru.sashasuper.logic;


import java.util.AbstractMap.SimpleEntry;
import java.util.List;

// Допытливый зритель, конечно, спросит: "Ну и нафига он нужен абстрактным"?
// Отвечаю: для возможной ленивой инициализации
public abstract class Dataset {
    public abstract List<SimpleEntry<Vector, Vector>> getAll();
}
