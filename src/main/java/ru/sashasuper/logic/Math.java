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
        return new Vector(rows,result);

    }

    public static float[] vectorMatrix(float[] vector, float[][] matrix) {

        int rows = matrix[0].length;
        int columns = matrix.length;

        float[] result = new float[rows];

        if (rows != columns) return null;

        for (int i=0; i<rows; i++) {
            float res = 0;
            for (int j=0; j < columns; j++) {
                res += vector[j] * matrix[j][i];
            }
            result[i] = res;
        }
        return result;
    }


}
