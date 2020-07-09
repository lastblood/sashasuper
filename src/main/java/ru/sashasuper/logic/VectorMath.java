package ru.sashasuper.logic;


import ru.sashasuper.logic.functions.ActivateFunction;

import java.util.function.BiFunction;

import static ru.sashasuper.utils.Assertions.thr;

public class VectorMath {

    //Умножение матрицы на вектор
    public static Vector multMatrixVector(Matrix matrix, Vector vector) {
        int rows = matrix.getRows();
        int columns = matrix.getColumns();

        thr(vector.getRealLength() != columns);

        float[] result = new float[rows];

        for (int i = 0; i < rows; i++) {
            float res = 0;
            for (int j = 0; j < columns; j++) {
                res += matrix.getValues()[i][j] * vector.getAllValues()[j];
            }
            result[i] = res;
        }
        return new Vector(result);
    }

    public static Vector multMatrixVectorTransposed(Matrix matrix, Vector vector) {
        int rows = matrix.getRows();
        int columns = matrix.getColumns();

        thr(vector.getRealLength() != rows);

        float[] result = new float[columns];

        for (int i = 0; i < columns; i++) {
            float res = 0;
            for (int j = 0; j < rows; j++) {
                res += matrix.getValues()[j][i] * vector.getAllValues()[j];
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
        int actualLength = v1.getRealLength();
        thr(actualLength != v2.getRealLength());

        Vector result = new Vector(new float[actualLength + (biased ? 1 : 0)]);
        for (int i = 0; i < actualLength; i++)
            result.getAllValues()[i] = operation.apply(v1.getAllValues()[i], v2.getAllValues()[i]);

        if (biased)
            result.getAllValues()[actualLength] = 1;

        return result;
    }


    //Произведение вектора-столбца на вектор-строку
    public static Matrix multVectors(Vector columnVector, Vector rowVector) {
        int columns = rowVector.getRealLength();
        int rows = columnVector.getRealLength();

        float[][] result = new float[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result[i][j] = columnVector.getAllValues()[i] * rowVector.getAllValues()[j];
            }
        }
        return new Matrix(result);
    }

    public static Vector applyToVector(Vector vector, ActivateFunction function) {
        return applyToVector(vector, function, false);
    }

    //Поэлементное применение функции активации (самой функции или производной)
    // todo: зачем здесь искусственный immutable? потанцевальная оптимизация
    public static Vector applyToVector(Vector vector, ActivateFunction function, boolean derivative) {
        int len = vector.getRealLength();
        Vector result = new Vector(new float[len]);

        for (int i = 0; i < len; i++) {
            float x = vector.getAllValues()[i];
            result.getAllValues()[i] = derivative ? function.derivative(x) : function.process(x);
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

    public static float MSE(Vector vector, Vector error) {
        thr(vector.getRealLength() != error.getRealLength());
        double sum = 0;
        for (int i = 0; i < vector.getRealLength(); i++) {
            float temp = vector.getAllValues()[i] - error.getAllValues()[i];
            sum += temp * temp;
        }
        return (float) (sum / vector.getRealLength());
    }
}

