package ru.sashasuper.logic.generators;

import ru.sashasuper.logic.Matrix;

import java.util.AbstractMap;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

import static ru.sashasuper.utils.Assertions.thr;

public class LambdaAllMatrixGenerator extends AbstractMatrixGenerator {

    private BiFunction<Integer, Integer, Matrix> internalMatrixGenerator;

    public LambdaAllMatrixGenerator(BiFunction<Integer, Integer, Matrix> matrixGenerator) {
        internalMatrixGenerator = matrixGenerator;
    }

    protected LambdaAllMatrixGenerator() { }

    @Override
    public Matrix generateMatrix(int rows, int columns) {
        return getInternalMatrixGenerator().apply(rows, columns);
    }

    @Override
    public Matrix[] generateMatrices(boolean biased, int ... sizes) {
        thr(sizes.length < 2,
                "Sizes should contains at least 2 values: input and output vectors' sizes");

        return IntStream.range(0, sizes.length-1)
                .mapToObj(x -> new AbstractMap.SimpleEntry<>(sizes[x+1], sizes[x] + (biased ? 1 : 0)))
                .map(y -> getInternalMatrixGenerator().apply(y.getKey(), y.getValue()))
                .toArray(Matrix[]::new);
    }

    protected BiFunction<Integer, Integer, Matrix> getInternalMatrixGenerator() {
        thr(internalMatrixGenerator == null);
        return internalMatrixGenerator;
    }
}
