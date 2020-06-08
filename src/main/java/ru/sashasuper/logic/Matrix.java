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

    public Matrix() {
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public float[][] getValues() {
        return values;
    }

    public void setValues(float[][] values) {
        this.values = values;
    }
}
