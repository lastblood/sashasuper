package ru.sashasuper.logic;

import java.io.Serializable;
import java.util.Arrays;

import static ru.sashasuper.utils.Assertions.thr;

// 04.08.20 Теперь матрицы всегда будут предназначены для работы с biased-векторами
// (а других и нет, лол), причем с обеих сторон. Это должно упростить работу с ними
// достаточно для того, чтоб использовать сторонние библиотеки
public class Matrix implements Serializable, Cloneable {
    private int rows, columns;
    private float[][] values;

    public Matrix(float[][] values) {
        thr(values.length == 0);
        rows = values.length + 1;
        columns = values[0].length;

        this.values = new float[rows][columns];
        System.arraycopy(values, 0, this.values, 0, values.length);

        int lastIndex = values.length;
        this.values[lastIndex] = new float[columns];
        this.values[lastIndex][0] = 1.0f;
    }

    public Matrix(float[][] values, boolean isAlreadyBiased) {
        thr(values.length == 0);
        rows = values.length + (isAlreadyBiased ? 0 : 1);
        columns = values[0].length;

        for(int i = 1; i < values.length; i++)
            thr(values[i].length != columns);

        this.values = new float[rows][columns];
        System.arraycopy(values, 0, this.values, 0, values.length);

        if(!isAlreadyBiased) {
            int lastIndex = values.length;
            this.values[lastIndex] = new float[columns];
            this.values[lastIndex][0] = 1.0f;
        }
    }

    private Matrix() {
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public float[][] getValues() {
        return values;
    }

    @Override
    public String toString() {
        return Arrays.deepToString(values);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        float[][] array = new float[getRows()][getColumns()];
        for(int i = 0; i < getRows(); i++)
            System.arraycopy(values[i], 0, array[i], 0, getColumns());

        return new Matrix(array, true);
    }

    public static Matrix getWithFixedBias(float[][] values) {
        Matrix m = new Matrix(values, true);
        float[] lastRow = m.values[m.values.length - 1];
        for (int i = 0; i < lastRow.length; i++)
            lastRow[i] = (i == 0) ? 1 : 0;
        return m;
    }
}
