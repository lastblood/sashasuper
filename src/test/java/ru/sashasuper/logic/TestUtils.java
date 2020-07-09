package ru.sashasuper.logic;

import static org.junit.jupiter.api.Assertions.*;

public class TestUtils {
    public static void assertMatricesEquals(Matrix matrix1, Matrix matrix2) {
        assertNotNull(matrix1);
        assertNotNull(matrix2);

        assertMatricesElementsEquals(matrix1.getValues(), matrix2.getValues());
    }

    public static void assertMatricesEquals(Matrix matrix, float[][] matrixElements) {
        assertNotNull(matrix);
        assertNotNull(matrixElements);

        assertMatricesElementsEquals(matrix.getValues(), matrixElements);
    }

    public static void assertMatricesElementsEquals(float[][] values1, float[][] values2) {
        assertNotNull(values1);
        assertNotNull(values2);

        assertEquals(values1.length, values2.length);
        for (int i = 0; i < values1.length; i++)
            assertArrayEquals(values1[i], values2[i]);
    }

    public static void assertMatricesEquals(Matrix matrix1, float oneByOneMatrix) {
        assertNotNull(matrix1);
        assertEquals(1, matrix1.getColumns());
        assertEquals(1, matrix1.getRows());
        assertEquals(oneByOneMatrix, matrix1.getValues()[0][0]);
    }


    public static void assertVectorsEquals(Vector vector1, Vector vector2) {
        assertNotNull(vector1);
        assertNotNull(vector2);
        assertEquals(vector1, vector2);
    }

    public static void assertVectorsEquals(Vector vector, float ... elements) {
        assertNotNull(vector);
        assertTrue(elements.length >= 1);
        assertVectorsEquals(vector, new Vector(elements));
    }
}
