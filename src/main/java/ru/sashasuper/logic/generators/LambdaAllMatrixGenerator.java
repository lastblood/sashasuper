package ru.sashasuper.logic.generators;

import ru.sashasuper.logic.Matrix;

import java.util.AbstractMap;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

import static ru.sashasuper.utils.Assertions.thr;

public class LambdaAllMatrixGenerator extends AbstractMatrixGenerator {

    private BiFunction<Integer, Integer, Matrix> matrixGenerator;

    public LambdaAllMatrixGenerator(BiFunction<Integer, Integer, Matrix> matrixGenerator) {
        this.matrixGenerator = matrixGenerator;
    }

    protected LambdaAllMatrixGenerator() { }

    @Override
    public Matrix generateMatrix(int rows, int columns) {
        return getMatrixGenerator().apply(rows, columns);
    }

    @Override
    public Matrix[] generateMatrices(boolean biased, int ... sizes) {
        // Из-за стримов происходит boxing в Integer, еще и SimpleEntry создаются,
        // так что оптимизация головного мозга не даст мне теперь уснуть

        thr(sizes.length < 2, "Sizes should contains at least 2 values: input and output vectors size");
        return IntStream.range(0, sizes.length-1)
                .mapToObj(x -> new AbstractMap.SimpleEntry<>(sizes[x+1], sizes[x] + (biased ? 1 : 0)))
                .map(y -> getMatrixGenerator().apply(y.getKey(), y.getValue()))
                .toArray(Matrix[]::new);
    }

    protected BiFunction<Integer, Integer, Matrix> getMatrixGenerator() {
        thr(matrixGenerator == null);
        return matrixGenerator;
    }
}
