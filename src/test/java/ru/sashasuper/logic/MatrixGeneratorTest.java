package ru.sashasuper.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class MatrixGeneratorTest {

    @Test
    void randomMatrixInRangeTest() {
        //Сгенерированные элементы матрицы лежат в указанном диапазоне
        MatrixGenerator mg = new MatrixGenerator();
        float min = 3.47f, max = 8.12f;
        Matrix matrix = mg.randomMatrix(600,90, min, max);
        float[][] values =  matrix.getValues();
        for (float[] value : values) {
            for (float v : value) {
                assertTrue(v >= min);
                assertTrue(v < max);
            }
        }
    }

    @Test
    void randomMatrixTest() {
        //Матрица имеет указанный размер
        MatrixGenerator generator = new MatrixGenerator();
        int rows = 7, columns = 4;
        Matrix matrix = generator.randomMatrix(rows, columns);
        assertEquals(matrix.getColumns(), columns);
        assertEquals(matrix.getRows(), rows);
        assertEquals(matrix.getValues().length, rows);
        assertEquals(matrix.getValues()[0].length, columns);
        assertNotNull(matrix.getValues());
    }
}