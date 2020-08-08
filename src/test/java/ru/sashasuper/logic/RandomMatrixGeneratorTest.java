package ru.sashasuper.logic;

import org.junit.jupiter.api.Test;
import ru.sashasuper.logic.generators.RandomMatrixGenerator;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;


class RandomMatrixGeneratorTest {

    void assertInRange(Matrix matrix, float min, float max) {
        float[][] values =  matrix.getValues();
        for (int i = 0; i < values.length - 1; i++) {
            for (float v : values[i]) {
                assertTrue(v >= min);
                assertTrue(v < max);
            }
        }
    }

    @Test
    void randomMatrixInRangeTest() {
        //Сгенерированные элементы матрицы лежат в указанном диапазоне
        RandomMatrixGenerator gen = new RandomMatrixGenerator();
        float min, max;

        min = 3.47f;
        max = 8.12f;
        assertInRange(gen.generateMatrix(600,90, min, max), min, max);

        min = -0.01f;
        max = 0.0001f;
        assertInRange(gen.generateMatrix(600,90, min, max), min, max);
    }

    @Test
    void randomMatrixInRangeDefaultTest() {
        float min = 0.1f, max = 0.3f;
        RandomMatrixGenerator generator = new RandomMatrixGenerator(min, max);
        assertInRange(generator.generateMatrix(60, 900), min, max);
    }

    @Test
    void randomMatricesInRangeTest() {
        RandomMatrixGenerator gen = new RandomMatrixGenerator();

        float min = -0.5f, max = 0.01f;
        Matrix[] matrices = gen.generateMatrices(min, max, 100, 50, 200);
        for (Matrix matrix : matrices) {
            assertInRange(matrix, min, max);
        }
    }

    @Test
    void randomMatricesDefaultInRangeTest() {
        float min = -0.1f, max = 5.01f;
        RandomMatrixGenerator gen = new RandomMatrixGenerator(min, max);

        Matrix[] matrices = gen.generateMatrices(100, 50, 200);
        for (Matrix matrix : matrices) {
            assertInRange(matrix, min, max);
        }
    }

    @Test
    void randomMatrixTest() {
        RandomMatrixGenerator generator = new RandomMatrixGenerator();
        int rows = 7, columns = 4;
        Matrix matrix = generator.generateMatrix(rows, columns);
        checkMatrixSize(matrix, rows, columns);
    }

    void checkMatrixSize(Matrix matrix, int rows, int columns) {
        assertEquals(matrix.getColumns(), columns);
        assertEquals(matrix.getRows(), rows + 1);
        assertNotNull(matrix.getValues());
    }

    @Test
    void randomMatricesOnesTest() {
        RandomMatrixGenerator generator = new RandomMatrixGenerator();
        Matrix[] matrices = generator.generateMatrices(1, 1, 1);
        assertTrue(matrices.length == 2);
        checkMatrixSize(matrices[0], 1, 2);
        checkMatrixSize(matrices[1], 1, 2);
    }
}