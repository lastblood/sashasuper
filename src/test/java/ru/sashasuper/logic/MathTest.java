package ru.sashasuper.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static ru.sashasuper.logic.Math.matrixVector;

class MathTest {

    @Test
    void matrixVectorTest1() {
        float[][] matrix = {{1,2}, {6, 56}};
        float[] vector = {4, 23};

        Matrix m = new Matrix(2, 2, matrix);
        Vector v = new Vector(2, vector);

        float[] result = {50, 1312};
        Vector res = matrixVector(m,v);
        assertArrayEquals(res.getValues(), result);
    }

    @Test
    void matrixVectorTest2() {
        float[][] matrix = {{0,0,-7}, {-1,0,0}};
        float[] vector = {0, 0, 2};

        Matrix m = new Matrix(2, 3, matrix);
        Vector v = new Vector(3, vector);

        float[] result = {-14, 0};
        Vector res = matrixVector(m,v);
        assertArrayEquals(res.getValues(), result);

    }

    @Test
    void matrixVectorTest3() {
        float[][] matrix = {{3,1,7,8}, {5,9,0,-4}};
        float[] vector = {3,4};

        Matrix m = new Matrix(2, 4, matrix);
        Vector v = new Vector(2, vector);

        try {
            Vector res = matrixVector(m, v);
            fail();
        } catch(Throwable t){

        }
    }


    @Test
    void vectorMatrixTest() {

    }
}