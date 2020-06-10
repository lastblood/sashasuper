package ru.sashasuper.logic;

import java.io.Serializable;

import static ru.sashasuper.utils.Assertions.thr;

public class Matrix implements Serializable {
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
}
