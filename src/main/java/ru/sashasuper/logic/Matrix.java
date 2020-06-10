package ru.sashasuper.logic;

import java.io.Serializable;

public class Matrix implements Serializable {
    private int rows, columns;
    private float[][] values;

    public Matrix(int rows, int columns, float[][] values) {
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
