package ru.sashasuper.logic;

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

    public IDXDataset(List<SimpleEntry<Vector, Integer>> lazyList) {
        thr(lazyList == null, "List in dataset is null");
        this.lazyList = lazyList;
    }

    public IDXDataset(List<SimpleEntry<Vector, Integer>> lazyList, int outputVectorLength) {
        thr(lazyList == null, "List in dataset is null");
        this.lazyList = lazyList;
        this.outputVectorLength = outputVectorLength;
    }

    private Vector vectorWithOneAtPosition(Integer value) {
        thr(value <= 0);

        float[] vectorValue = new float[outputVectorLength];
        vectorValue[value] = 1.0f;
        return new Vector(vectorValue);
    }

    @Override
    public List<SimpleEntry<Vector, Vector>> getAll() {
        if(processedList == null) {
            thr(lazyList == null, "List in dataset is null");
            processedList = new ArrayList<>(lazyList.size());

            for (SimpleEntry<Vector, Integer> entry : lazyList)
                processedList.add(new SimpleEntry<>(entry.getKey(), vectorWithOneAtPosition(entry.getValue())));

            lazyList = null;
        }

        return processedList;
    }
}
