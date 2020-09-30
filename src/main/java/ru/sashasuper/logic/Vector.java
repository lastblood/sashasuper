package ru.sashasuper.logic;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static ru.sashasuper.utils.Assertions.thr;

public class Vector implements Serializable, Iterable<Float> {
    // Содержит values.length-1 "значащих" элементов и сдвиг на последнем
    // сдвиг не обязан быть 1, но любая сторонняя функция вправе его изменить
    private float[] values;

    // Всегда считает, что values - non-biased и всегда делает из него biased
    public Vector(float ... values) {
        thr(values.length < 1);
        this.values = makeItBiased(values);
    }

    // isBiased == true -> массив записывается без изменений
    // isBiased == false -> массив изменяется согласно контракту класса
    // Т.о. флажок true значит, что пользователь берет на себя ответственность за bias
    public Vector(boolean isBiased, float ... values) {
        thr(values.length < (isBiased ? 2 : 1), "Array is too small");
        this.values = isBiased ? values : makeItBiased(values);
    }

    public static float[] makeItBiased(float[] values) {
        float[] result = new float[values.length + 1];
        System.arraycopy(values, 0, result, 0, values.length);
        result[values.length] = 1;
        return result;
    }

    private Vector() {
    }

    // Всегда non-biased вне зависимости от `biased`
    public int getNonBiasedLength() {
        return values.length - 1;
    }

    public int getFullLen() {
        return values.length;
    }

    public int getLength(boolean biased) {
        return values.length - (biased ? 0 : 1);
    }

    public float[] getValues() {
        return values;
    }


    @Override
    public String toString() {
        return Arrays.toString(values);
    }

    // Игнорирует biased
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Vector vector = (Vector) o;

        if(vector.getNonBiasedLength() == getNonBiasedLength())
            for (int i = 0; i < getNonBiasedLength(); i++)
                if(vector.values[i] != values[i])
                    return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = 0x811c9dc5;
        for (int i = 0; i < getNonBiasedLength(); i++)
            result = (result * 0x01000193) ^ Float.floatToIntBits(values[i]);

        return result;
    }

    // Для условно-бесплатного обхода по элементам вектора без байеса
    @Override
    public Iterator<Float> iterator() {
        return new Iterator<>() {
            private int iteratorPosition = 0;

            @Override
            public boolean hasNext() {
                return iteratorPosition < getNonBiasedLength();
            }

            @Override
            public Float next() {
                if (!hasNext()) throw new NoSuchElementException();
                return values[iteratorPosition++];
            }
        };
    }
}

