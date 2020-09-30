package ru.sashasuper.logic;

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

import static ru.sashasuper.utils.Assertions.thr;

public class VectorMath {

    //Умножение матрицы на вектор
    public static Vector multMatrixVector(Matrix matrix, Vector vector, boolean biased) {
        int rows = matrix.getRows();
        int columns = matrix.getColumns();

        thr(vector.getLength(biased) != columns);

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

    public static Vector multMatrixVectorTransposed(Matrix matrix, Vector vector, boolean biased) {
        int rows = matrix.getRows();
        int columns = matrix.getColumns() - (biased ? 1 : 0);

        thr(vector.getNonBiasedLength() != rows);

        float[] result = new float[columns];

        for (int i = 0; i < columns; i++) {
            float res = 0;
            for (int j = 0; j < rows; j++) {
                res += matrix.getValues()[j][i] * vector.getValues()[j];
            }
            result[i] = res;
        }
        return new Vector(result);
    }

    //Поэлементное умножение векторов
    public static Vector multElements(Vector firstVector, Vector secondVector, boolean biasedResult) {
        return binaryVectorOperation(firstVector, secondVector, (x,y) -> x * y, biasedResult);
    }

    //Поэлементное вычитание векторов
    public static Vector subElements(Vector firstVector, Vector secondVector, boolean biasedResult) {
        return binaryVectorOperation(firstVector, secondVector, (x,y) -> x - y, biasedResult);
    }

    // Универсальная поэлементная операция с оптимизированным созданием bias-векторов
    private static Vector binaryVectorOperation(
                Vector v1, Vector v2, BiFunction<Float, Float, Float> operation, boolean biased) {

        int actualLength = v1.getNonBiasedLength();
        thr(actualLength != v2.getNonBiasedLength());

        Vector result = new Vector(true, new float[actualLength + 1]);
        for (int i = 0; i < actualLength; i++)
            result.getValues()[i] = operation.apply(v1.getValues()[i], v2.getValues()[i]);

        result.getValues()[actualLength] = 1;

        return result;
    }


    //Произведение вектора-столбца на вектор-строку
    public static Matrix multVectors(Vector columnVector, Vector rowVector, boolean biased) {
        int columns = rowVector.getNonBiasedLength();
        int rows = columnVector.getLength(biased);

        float[][] result = new float[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result[i][j] = columnVector.getValues()[i] * rowVector.getValues()[j];
            }
        }
        return new Matrix(result);
    }

    // Поэлементное сложение матриц
    public static Matrix addMatrices(Matrix firstMatrix, Matrix secondMatrix) {
        return binaryMatrixOperation(firstMatrix, secondMatrix, (a,b) -> a + b);
    }

    // Поэлементное вычитание матриц
    public static Matrix subMatrices(Matrix firstMatrix, Matrix secondMatrix) {
        return binaryMatrixOperation(firstMatrix, secondMatrix, (a,b) -> a - b);
    }

    public static Matrix binaryMatrixOperation(Matrix first, Matrix second, BinaryOperator<Float> op) {
        int columns = first.getColumns(), rows = first.getRows();
        thr(columns != second.getColumns() || rows != second.getRows());

        float[][] valuesResult = new float[rows][columns];
        float[][] values1 = first.getValues();
        float[][] values2 = second.getValues();
        for (int y = 0; y < rows; y++)
            for (int x = 0; x < columns; x++)
                valuesResult[y][x] = op.apply(values1[y][x], values2[y][x]);

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

    public static float MSE(Vector ideal, Vector output) {
        int length = ideal.getNonBiasedLength();
        thr(length != output.getNonBiasedLength());

        double sum = 0;
        for (int i = 0; i < length; i++) {
            float temp = ideal.getValues()[i] - output.getValues()[i];
            sum += temp * temp;
        }
        return (float) (sum / length);
    }

    public static float CE(Vector ideal, Vector output) {
        int length = ideal.getNonBiasedLength();
        thr(length != output.getNonBiasedLength());

        double sum = 0;
        for (int i = 0; i < length; i++) {
            float y = ideal.getValues()[i];
            float a = output.getValues()[i];
            double result = y * Math.log(a) + (1 - y) * Math.log(1 - a);
            sum += Double.isNaN(result) ? 0 : result;
        }
        return (float) (sum / -length);
    }
}

