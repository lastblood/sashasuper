package ru.sashasuper.logic;

import java.util.Random;

public class MatrixGenerator {
    private Random random = null;

    public MatrixGenerator() {
    }

    public MatrixGenerator(Random random) {
        this.random = random;
    }

    public Matrix randomMatrixInRange(int rows, int columns, float min, float max) {
        Random r = (random == null) ? new Random() : random;

        float values[][] = new float[rows][columns];

        for(int y = 0; y < rows; y++) {
            for(int x = 0; x < columns; x++) {
                values[y][x] = min + r.nextFloat() * (max - min);
            }
        }

        return new Matrix(rows, columns, values);
    }

    public Matrix randomMatrix(int rows, int columns) {
        return randomMatrixInRange(rows, columns, -1, 1);
    }
}