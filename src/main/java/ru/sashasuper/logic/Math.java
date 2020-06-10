package ru.sashasuper.logic;


import static ru.sashasuper.utils.Assertions.thr;

public class Math {

    public static Vector matrixVector(Matrix matrix, Vector vector) {
        int rows = matrix.getRows();
        int columns = matrix.getColumns();

        thr(vector.getLen() != columns);

        float[] result = new float[rows];

        for (int i = 0; i < rows; i++) {
            float res = 0;
            for (int j = 0; j < columns; j++) {
                res += matrix.getValues()[i][j] * vector.getValues()[j];
            }
            result[i] = res;
        }
        return new Vector(rows, result);

    }

    public static Vector mult(Vector firstVector, Vector secondVector) {

        thr(firstVector.getLen() != secondVector.getLen());

        Vector result = new Vector(firstVector.getLen(), new float[firstVector.getLen()]);
        for (int i = 0; i < result.getLen(); i++)
            result.getValues()[i] = firstVector.getValues()[i] * secondVector.getValues()[i];
        return result;
    }

    public static Vector sub(Vector firstVector, Vector secondVector) {

        thr(firstVector.getLen() != secondVector.getLen());

        Vector result = new Vector(firstVector.getLen(), new float[firstVector.getLen()]);
        for (int i = 0; i < result.getLen(); i++)
            result.getValues()[i] = firstVector.getValues()[i] - secondVector.getValues()[i];
        return result;

    }

    public static Matrix vMult(Vector column, Vector row) {

        int rows = row.getLen();
        int columns = column.getLen();

        thr(columns != rows);

        float[][] result = new float[columns][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result[i][j] = column.getValues()[i] * row.getValues()[j];
            }
        }
        return new Matrix(rows, columns, result);
    }
}




