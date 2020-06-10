package ru.sashasuper.logic;


import ru.sashasuper.logic.functions.ActivateFunction;

import static ru.sashasuper.utils.Assertions.thr;

public class VectorMath {

    //Умножение матрицы на вектор
    public static Vector multMatrixVector(Matrix matrix, Vector vector) {
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

    //Поэлементное умножение векторов
    public static Vector multElements(Vector firstVector, Vector secondVector) {

        thr(firstVector.getLen() != secondVector.getLen());

        Vector result = new Vector(firstVector.getLen(), new float[firstVector.getLen()]);
        for (int i = 0; i < result.getLen(); i++)
            result.getValues()[i] = firstVector.getValues()[i] * secondVector.getValues()[i];
        return result;
    }

    //Поэлементное вычитание векторов
    public static Vector subElements(Vector firstVector, Vector secondVector) {

        thr(firstVector.getLen() != secondVector.getLen());

        Vector result = new Vector(firstVector.getLen(), new float[firstVector.getLen()]);
        for (int i = 0; i < result.getLen(); i++)
            result.getValues()[i] = firstVector.getValues()[i] - secondVector.getValues()[i];
        return result;

    }

    //Произведение вектора-столбца на вектор-строку
    public static Matrix multVectors(Vector columnVector, Vector rowVector) {

        int rows = rowVector.getLen();
        int columns = columnVector.getLen();

        thr(columns != rows);

        float[][] result = new float[columns][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result[i][j] = columnVector.getValues()[i] * rowVector.getValues()[j];
            }
        }
        return new Matrix(rows, columns, result);
    }

    //Поэлементное применение функции активации
    public static Vector applyToVector(Vector vector, ActivateFunction function) {
        int len = vector.getLen();
        Vector result = new Vector(len, new float[len]);

        for (int i = 0; i < len; i++)
            result.getValues()[i] = function.process(vector.getValues()[i]);

        return result;
    }
}




