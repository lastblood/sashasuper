package ru.sashasuper.logic.generators;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;

public class XavierGenerator extends LambdaElementMatrixGenerator {
    private XavierGenerator(BiFunction<Integer, Integer, Float> elementGenerator) {
        super(elementGenerator);
    }

    public XavierGenerator() {
        super((rows, columns) ->
                (ThreadLocalRandom.current().nextFloat() * 2 - 1) * (float) Math.sqrt(6f/(rows+columns)));

    }
}
