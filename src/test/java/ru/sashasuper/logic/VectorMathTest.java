package ru.sashasuper.logic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static ru.sashasuper.logic.VectorMath.*;

class VectorMathTest {

    @Test
    void multMatrixVectorTest1() {
        float[][] matrix = {{1,2}, {6, 56}};
        float[] vector = {4, 23};

        Matrix m = new Matrix(matrix);
        Vector v = new Vector(vector);

        float[] result = {50, 1312};
        Vector res = multMatrixVector(m,v);
        assertArrayEquals(res.getValues(), result);
    }

    @Test
    void matrixVectorTest2() {
        float[][] matrix = {{0,0,-7}, {-1,0,0}};
        float[] vector = {0, 0, 2};

        Matrix m = new Matrix(matrix);
        Vector v = new Vector(vector);

        float[] result = {-14, 0};
        Vector res = multMatrixVector(m,v);
        assertArrayEquals(res.getValues(), result);

    }

    @Test
    void matrixVectorTest3() {
        float[][] matrix = {{3,1,7,8}, {5,9,0,-4}};
        float[] vector = {3,4};

        Matrix m = new Matrix(matrix);
        Vector v = new Vector(vector);

        try {
            Vector res = multMatrixVector(m, v);
            fail();
        } catch(Throwable t){

        }
    }

    @Test
    void multElementsTest1() {
        float[] firstVector = {0, 0, 2};
        float[] secondVector = {1, 2, 3};

        Vector v1 = new Vector(firstVector);
        Vector v2 = new Vector(secondVector);

        float[] result = {0, 0, 6};
        Vector res = multElements(v1, v2);
        assertArrayEquals(res.getValues(), result);
    }

    @Test
    void multElementsTest2() {
        float[] firstVector = {0, -4, 2};
        float[] secondVector = {1, 2};

        Vector v1 = new Vector(firstVector);
        Vector v2 = new Vector(secondVector);

        try {
            Vector res = multElements(v1, v2);
            fail();
        } catch (Throwable t) {

        }
    }

    @Test
    void subElementsTest1() {
        float[] fVector = {1, 32, -4};
        float[] sVector = {4, 22, 0};

        Vector v1 = new Vector(fVector);
        Vector v2 = new Vector(sVector);

        float[] result = {-3, 10, -4};
        Vector res = subElements(v1, v2);
        assertArrayEquals(res.getValues(), result);
    }

    @Test
    void subtractionTest2() {
        float[] fVector = {1, 32, -22, 0, 1, 4};
        float[] sVector = {4, 5};

        Vector v1 = new Vector(fVector);
        Vector v2 = new Vector(sVector);

        try {
            Vector res = subElements(v1, v2);
            fail();
        } catch (Throwable t) {

        }

    }

    @Test
    void multVectorsTest1() {
        float[] column = {1, -1, 3};
        float[] row = {0, -5, 1};

        Vector v1 = new Vector(column);
        Vector v2 = new Vector(row);

        float[][] result = {{0, -5, 1}, {-0.0f, 5, -1}, {0, -15, 3}};
        Matrix res = multVectors(v1, v2);
        assertArrayEquals(res.getValues(), result);
    }

    @Test
    void multVectorsTest2() {
        float[] column = {4,3,6,7};
        float[] row = {9,11,2};

        Vector v1 = new Vector(column);
        Vector v2 = new Vector(row);

        try {
            Matrix res = multVectors(v1, v2);
            fail();
        } catch (Throwable t) {

        }
    }
}