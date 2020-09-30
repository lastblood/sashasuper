package ru.sashasuper.logic.generators;

import ru.sashasuper.logic.Matrix;

import java.util.function.BiFunction;

public class LambdaElementMatrixGenerator extends LambdaAllMatrixGenerator {

    private BiFunction<Integer, Integer, Matrix> internalMatrixGenerator;

    @Override
    protected BiFunction<Integer, Integer, Matrix> getInternalMatrixGenerator() {
        return internalMatrixGenerator;
    }

    public LambdaElementMatrixGenerator(BiFunction<Integer, Integer, Float> elementGenerator) {
        internalMatrixGenerator = ((rows, columns) -> {
            float[][] values = new float[rows][columns];
            for(int y = 0; y < rows; y++)
                for(int x = 0; x < columns; x++)
                    values[y][x] = elementGenerator.apply(rows, columns);

            return new Matrix(values);
        });
    }
}
