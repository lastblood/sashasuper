package ru.sashasuper.logic;


import ru.sashasuper.logic.functions.ActivateFunction;
import ru.sashasuper.logic.functions.ElementFunction;

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
        return new Vector(true, result);
    }

    public static Vector multMatrixVectorTransposed(Matrix matrix, Vector vector, boolean biased) {
        int rows = matrix.getRows() - 1;
        int columns = matrix.getColumns() - (biased ? 1 : 0);

//        System.out.println("rows = " + rows);
//        System.out.println("columns = " + columns);
//
//        System.out.println("matrix = " + matrix);
//        System.out.println("vector = " + vector);

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
//        System.out.println("firstVector = " + firstVector);
//        System.out.println("secondVector = " + secondVector);
        return binaryVectorOperation(firstVector, secondVector, (x,y) -> x * y, biasedResult);
    }

    //Поэлементное вычитание векторов
    public static Vector subElements(Vector firstVector, Vector secondVector, boolean biasedResult) {
        return binaryVectorOperation(firstVector, secondVector, (x,y) -> x - y, biasedResult);
    }

    // Универсальная поэлементная операция с оптимизированным созданием bias-векторов
    // todo: есть ли разница между biased и non-biased?
    private static Vector binaryVectorOperation(
                Vector v1, Vector v2, BiFunction<Float, Float, Float> operation, boolean biased) {

//        int actualLength = v1.getLength(biased);
//        thr(actualLength != v2.getLength(biased));

        int length = v1.getFullLen();
        thr(length != v2.getFullLen());

        Vector result = new Vector(true, new float[length]);
        for (int i = 0; i < length - 1; i++)
            result.getValues()[i] = operation.apply(v1.getValues()[i], v2.getValues()[i]);

        result.getValues()[length - 1] = 1;

        return result;
    }


    //Произведение вектора-столбца на вектор-строку
    // todo: оптимизировать под создание сразу biased-версии матрицы
    public static Matrix multVectors(Vector columnVector, Vector rowVector, boolean biased) {
//        int columns = rowVector.getRealLength();
//        int rows = columnVector.getRealLength();

//        System.out.println("columnVector = " + columnVector);
//        System.out.println("rowVector = " + rowVector);

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

    public static Vector applyToVector(Vector vector, ElementFunction function) {
        return applyToVector(vector, function, false);
    }

    //Поэлементное применение функции активации (самой функции или производной)
    // todo: зачем здесь искусственный immutable? потанцевальная оптимизация
    public static Vector applyToVector(Vector vector, ElementFunction function, boolean derivative) {
        int len = vector.getNonBiasedLength();
        Vector result = new Vector(new float[len]);

        if(derivative) {
            ActivateFunction f = (ActivateFunction) function;
            for (int i = 0; i < len; i++) {
                float x = vector.getValues()[i];
                result.getValues()[i] = f.derivative(x);
            }
        } else {
            for (int i = 0; i < len; i++) {
                float x = vector.getValues()[i];
                result.getValues()[i] = function.process(x);
            }
        }

        return result;
    }

    public static Matrix addMatrices(Matrix firstMatrix, Matrix secondMatrix) {
        return matrixBinaryOperator(firstMatrix, secondMatrix, (x,y) -> x + y);
    }

    public static Matrix subMatrices(Matrix firstMatrix, Matrix secondMatrix) {
        return matrixBinaryOperator(firstMatrix, secondMatrix, (x,y) -> x - y);
    }

    // Поэлементное вычитание матриц
    public static Matrix matrixBinaryOperator(Matrix first, Matrix second, BinaryOperator<Float> operator) {
        int columns = first.getColumns(), rows = first.getRows() - 1;
        thr(columns != second.getColumns() || rows != second.getRows() - 1);

        float[][] valuesResult = new float[rows][columns];
        float[][] values1 = first.getValues();
        float[][] values2 = second.getValues();
        for (int y = 0; y < rows; y++)
            for (int x = 0; x < columns; x++)
                valuesResult[y][x] = operator.apply(values1[y][x], values2[y][x]);
//                valuesResult[y][x] = values1[y][x] - values2[y][x];

        return new Matrix(valuesResult);
    }

    // Транспонирование матрицы и умножение на число
    public static Matrix multMatrixByT(Matrix matrix, float multiplier) {
        int columns = matrix.getColumns(), rows = matrix.getRows() - 1;

        float[][] newValue = new float[columns][rows], oldValues = matrix.getValues();
        for (int x = 0; x < columns; x++)
            for (int y = 0; y < rows; y++)
                newValue[x][y] = oldValues[y][x] * multiplier;

        return new Matrix(newValue);
    }

    public static float MSE(Vector vector, Vector error) {
        int length = vector.getNonBiasedLength();
        thr(length != error.getNonBiasedLength());

        double sum = 0;
        for (int i = 0; i < length; i++) {
            float temp = vector.getValues()[i] - error.getValues()[i];
            sum += temp * temp;
        }
        return (float) (sum / length);
    }
}

