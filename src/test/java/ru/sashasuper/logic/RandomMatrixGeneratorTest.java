package ru.sashasuper.logic;

import org.junit.jupiter.api.Test;
import ru.sashasuper.logic.generators.RandomMatrixGenerator;
import ru.sashasuper.logic.neural.Matrix;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;


class RandomMatrixGeneratorTest {

    @Test
    void randomMatrixInRangeTest() {
        //Сгенерированные элементы матрицы лежат в указанном диапазоне
        RandomMatrixGenerator mg = new RandomMatrixGenerator();
        float min = 3.47f, max = 8.12f;
        Matrix matrix = mg.generateMatrix(600,90, min, max);
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
        RandomMatrixGenerator generator = new RandomMatrixGenerator();
        int rows = 7, columns = 4;
        Matrix matrix = generator.generateMatrix(rows, columns);
        checkMatrixSize(matrix, rows, columns);
    }

    void checkMatrixSize(Matrix matrix, int rows, int columns) {
        assertEquals(matrix.getColumns(), columns);
        assertEquals(matrix.getRows(), rows);
        assertNotNull(matrix.getValues());
        assertEquals(matrix.getValues().length, rows);
        assertEquals(matrix.getValues()[0].length, columns);
    }

    @Test
    void randomMatricesOnesTest() {
        RandomMatrixGenerator generator = new RandomMatrixGenerator();
        Matrix[] matrices = generator.generateMatrices(true, 1, 1, 1);
        System.out.println(Arrays.toString(matrices));
        assertTrue(matrices.length == 2);
        checkMatrixSize(matrices[0], 1, 2);
        checkMatrixSize(matrices[1], 1, 2);
    }
}