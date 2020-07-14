package ru.sashasuper.logic.neural;

import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.util.*;

import static ru.sashasuper.utils.Assertions.thr;


// 09.07.20 Отныне векторы всегда будут хранить в себе на единицу больше памяти,
// вне зависимости от того, используют ли они сдвиг или нет. При этом класс
// не гарантирует, что последний элемент, bias, будет содержать единицу, однако
// он не будет сравниваться при вызове метода equals, не будет учитываться при
// сравнении, и его разрешается спокойно изменять любой функции извне

public class Vector implements Serializable, Iterable {
    // Содержит values.length-1 "значащих" элементов и сдвиг на последнем
    // сдвиг не обязан быть 1, но любая сторонняя функция вправе его изменить
    private float[] values;

    // Всегда считает, что values - non-biased и всегда делает из него biased
    public Vector(float ... values) {
        thr(values.length < 1);
        this.values = makeItBiased(values);
    }

    // isBiased == true -> массив записывается без изменений, biased=true
    // isBiased == false -> массив изменяется согласно контракту класса, biased=false
    // Т.о. при true, пользователь API берет на себя ответственность за наличие места под bias
    public Vector(boolean isBiased, float ... values) {
        thr(values.length < (isBiased ? 2 : 1), "Недостаточная длина вектора для создания");
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

    public void doBias() {
        values[values.length - 1] = 1;
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

    // Срезы не завезли, так что создаем копию без Bias и кэшируем мягкой ссылкой
    // todo: а зочем?
    private SoftReference<float[]> nonBiasedValues = null;
    public float[] getNonBiasedValues() {
        if(nonBiasedValues == null || nonBiasedValues.get() == null) {
            float[] croppedArray = new float[values.length - 1];
            System.arraycopy(values, 0, croppedArray, 0, croppedArray.length);
            nonBiasedValues = new SoftReference<>(croppedArray);
        }

        thr(nonBiasedValues.get() == null);
        return nonBiasedValues.get();
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

    @Override
    public Iterator iterator() {
        return new Iterator() {
            private int iter = 0;

            @Override
            public boolean hasNext() {
                return iter < getNonBiasedLength();
            }

            @Override
            public Object next() {
                if(!hasNext()) throw new NoSuchElementException();
                return values[iter++];
            }
        };
    }
}


