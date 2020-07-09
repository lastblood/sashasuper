package ru.sashasuper.logic;

import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static ru.sashasuper.utils.Assertions.thr;


// 09.07.20 Отныне векторы всегда будут хранить в себе на единицу больше памяти,
// вне зависимости от того, используют ли они сдвиг или нет. Это сделано для
// упрощения API, который теперь не должен учитывать разные длины массива внутри
// Vector, в зависимости от флажка, и для оптимизаций работы с памятью: лучше
// один раз скопировать массив при создании, чем постоянно потом гонять туда-сюда.
// Т.о. теперь переход от biased к non-biased версии Vector не требует копирования
// содержимого массива, а только изменения флажка, либо создания объекта-копии с
// измененным только лишь флажком (небезопасно для изменения данных, но что поделать)

public class Vector implements Serializable, Iterable {
    // Содержит values.length-1 "значащих" элементов и сдвиг на последнем
    // сдвиг не обязан быть 1, но любая сторонняя функция вправе его изменить
    private float[] values;

    // теперь не влияет на содержимое массива, как и на
    private boolean biased = false;

    // Всегда считает, что values - non-biased и всегда делает из него biased
    public Vector(float ... values) {
        thr(values.length < 1);
        this.values = makeItBiased(values);
        this.biased = true;
    }

    // isBiased == true -> массив записывается без изменений, biased=true
    // isBiased == false -> массив изменяется согласно контракту класса, biased=false
    // Т.о. при true, пользователь API берет на себя ответственность за наличие места под bias
    public Vector(boolean isBiased, float ... values) {
        thr(values.length < (isBiased ? 2 : 1));
        this.biased = isBiased;
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

    // Длину со сдвигом, если biased, без сдвига - если non-biased
    public int getRealLength() {
        return values.length - (isBiased() ? 1 : 0);
    }

    public int getFullLen() {
        return values.length;
    }



    public float[] getAllValues() {
        return values;
    }

    // Срезы не завезли, так что создаем копию без Bias и кэшируем мягкой ссылкой
    // todo: а зочем?
    private SoftReference<float[]> nonBiasedValues = null;
    public float[] getNonBiasedValues() {
        if(!isBiased()) {
            return getAllValues();
        } else if(nonBiasedValues == null || nonBiasedValues.get() == null) {
            float[] croppedArray = new float[values.length - 1];
            System.arraycopy(values, 0, croppedArray, 0, croppedArray.length);
            nonBiasedValues = new SoftReference<>(croppedArray);
        }

        thr(nonBiasedValues.get() == null);
        return nonBiasedValues.get();
    }

    public boolean isBiased() {
        return biased;
    }


    // Всегда возвращает biased-версию, считая, что ему передан массив только
    // значащих элементов, поэтому копирует его в новый, на единичку больший
    public static Vector of(float ... values) {
        return new Vector(true, makeItBiased(values));
    }

    // Всегда возвращает biased-версию этого vector
    // (всегда тот же внутренний массив, и потенциально тот же объект Vector)
    public static Vector of(Vector vector) {
        return vector.isBiased() ? vector : new Vector(true, vector.values);
    }



    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Vector{");
        sb.append("values=").append(Arrays.toString(values)).append('}');
        return sb.toString();
    }

    // Игнорирует biased
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Vector vector = (Vector) o;

        if(vector.getRealLength() == getRealLength())
            for (int i = 0; i < getRealLength(); i++)
                if(vector.values[i] != values[i])
                    return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = 0x811c9dc5;
        for (int i = 0; i < getRealLength(); i++)
            result = (result * 0x01000193) ^ Float.floatToIntBits(values[i]);

        return result;
    }

    @Override
    public Iterator iterator() {
        return new Iterator() {
            private int iter = 0;

            @Override
            public boolean hasNext() {
                return iter < getRealLength();
            }

            @Override
            public Object next() {
                if(!hasNext()) throw new NoSuchElementException();
                return values[iter++];
            }
        };
    }
}


