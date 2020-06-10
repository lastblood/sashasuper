package ru.sashasuper.logic;

import java.io.Serializable;

import static ru.sashasuper.utils.Assertions.thr;

public class Matrix implements Serializable {
    private int rows, columns;
    private float[][] values;

    public Matrix(int rows, int columns, float[][] values) {
        thr(rows <= 0 || columns <= 0);
        thr(values.length != rows);
        for(int i = 0; i < rows; i++) thr(values[i].length != columns);

        this.rows = rows;
        this.columns = columns;
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
