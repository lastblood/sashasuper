package ru.sashasuper.logic.neural;

import java.io.Serializable;
import java.util.Arrays;

import static ru.sashasuper.utils.Assertions.thr;

public class Matrix implements Serializable, Cloneable {
    private int rows, columns;
    private float[][] values;

    public Matrix(float[][] values) {
        thr(values.length == 0);
        rows = values.length;
        columns = values[0].length;
        for(int i = 1; i < values.length; i++)
            thr(values[i].length != columns);

        this.values = values;
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
//        return new Matrix(getValues().clone());
        float[][] array = new float[getRows()][getColumns()];

        for(int i = 0; i < getRows(); i++) {
            for(int j = 0; j < getColumns(); j++) {
                array[i][j] = values[i][j];
            }
        }

        return new Matrix(array);
    }
}
