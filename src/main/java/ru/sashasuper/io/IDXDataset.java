package ru.sashasuper.io;

import ru.sashasuper.logic.Vector;

import java.util.ArrayList;
import java.util.List;

import static java.util.AbstractMap.SimpleEntry;
import static ru.sashasuper.utils.Assertions.thr;

// Ленивая версия, хранящая изначально выходные векторы как числа
// При первом вызове пересчитывает и в дальнейшем возвращает уже запомненный результат
public class IDXDataset extends Dataset {
    private List<SimpleEntry<Vector, Vector>> processedList = null;
    private List<SimpleEntry<Vector, Integer>> lazyList;
    private int outputVectorLength = 10;

    private static final String nulMessage = "List in dataset is null";

    public IDXDataset(List<SimpleEntry<Vector, Integer>> lazyList) {
        thr(lazyList == null, nulMessage);
        this.lazyList = lazyList;
    }

    public IDXDataset(List<SimpleEntry<Vector, Integer>> lazyList, int outputVectorLength) {
        thr(lazyList == null, nulMessage);
        this.lazyList = lazyList;
        this.outputVectorLength = outputVectorLength;
    }

    // Возвращает вектор с нулями на всех позициях кроме [value+1]
    public static Vector vectorWithOneAtPosition(int value, int length) {
        thr(value <= length);
        float[] vectorValue = new float[length];
        vectorValue[value] = 1.0f;
        return new Vector(vectorValue);
    }

    private Vector vectorWithOneAtPosition(Integer value) {
        return vectorWithOneAtPosition(value, outputVectorLength);
    }

    @Override
    public List<SimpleEntry<Vector, Vector>> getAll() {
        if(processedList == null) {
            thr(lazyList == null, nulMessage);
            processedList = new ArrayList<>(lazyList.size());

            for (SimpleEntry<Vector, Integer> entry : lazyList)
                processedList.add(new SimpleEntry<>(entry.getKey(), vectorWithOneAtPosition(entry.getValue())));

            lazyList = null;
        }

        return processedList;
    }
}
