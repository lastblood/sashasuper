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
        return new Vector(result);

    }

    //Поэлементное умножение векторов
    public static Vector multElements(Vector firstVector, Vector secondVector) {

        thr(firstVector.getLen() != secondVector.getLen());

        Vector result = new Vector(new float[firstVector.getLen()]);
        for (int i = 0; i < result.getLen(); i++)
            result.getValues()[i] = firstVector.getValues()[i] * secondVector.getValues()[i];
        return result;
    }

    //Поэлементное вычитание векторов
    public static Vector subElements(Vector firstVector, Vector secondVector) {

        thr(firstVector.getLen() != secondVector.getLen());

        Vector result = new Vector(new float[firstVector.getLen()]);
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
        return new Matrix(result);
    }

    public static Vector applyToVector(Vector vector, ActivateFunction function) {
        return applyToVector(vector, function, false);
    }

    //Поэлементное применение функции активации (самой функции или производной)
    public static Vector applyToVector(Vector vector, ActivateFunction function, boolean derivative) {
        int len = vector.getLen();
        Vector result = new Vector(new float[len]);

        for (int i = 0; i < len; i++) {
            float x = vector.getValues()[i];
            result.getValues()[i] = derivative ? function.derivative(x) : function.process(x);
        }

        return result;
    }

    // Поэлементное вычитание матриц
    public static Matrix subMatrices(Matrix firstMatrix, Matrix secondMatrix) {
        int columns = firstMatrix.getColumns(), rows = firstMatrix.getRows();
        thr(columns != secondMatrix.getColumns() || rows != secondMatrix.getRows());

        float[][] valuesResult = new float[rows][columns];
        float[][] values1 = firstMatrix.getValues();
        float[][] values2 = secondMatrix.getValues();
        for (int y = 0; y < rows; y++)
            for (int x = 0; x < columns; x++)
                valuesResult[y][x] = values1[y][x] - values2[y][x];

        return new Matrix(valuesResult);
    }

    // Транспонирование матрицы и умножение на число
    public static Matrix multMatrixByT(Matrix matrix, float multiplier) {
        int columns = matrix.getColumns(), rows = matrix.getRows();

        float[][] newValue = new float[columns][rows], oldValues = matrix.getValues();
        for (int x = 0; x < columns; x++)
            for (int y = 0; y < rows; y++)
                newValue[x][y] = oldValues[y][x] * multiplier;

        return new Matrix(newValue);
    }
}

