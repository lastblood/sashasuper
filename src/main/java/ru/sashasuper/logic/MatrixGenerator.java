package ru.sashasuper.logic;

public abstract class MatrixGenerator {
    abstract public Matrix generateMatrix(int rows, int columns);
    abstract public Matrix[] generateMatrices(boolean biased, int ... sizes);
}
