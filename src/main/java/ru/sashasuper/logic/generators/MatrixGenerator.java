package ru.sashasuper.logic.generators;

import ru.sashasuper.logic.Matrix;

public abstract class MatrixGenerator {
    abstract public Matrix generateMatrix(int rows, int columns);
    abstract public Matrix[] generateMatrices(int ... sizes);
}
