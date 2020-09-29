package ru.sashasuper.logic.generators;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;

public class NormalDistributionGenerator extends LambdaElementMatrixGenerator {
    private NormalDistributionGenerator(BiFunction<Integer, Integer, Float> elementGenerator) {
        super(elementGenerator);
    }

    public NormalDistributionGenerator(float sigma) {
        super ((rows, columns) -> (float) ThreadLocalRandom.current().nextGaussian() *  sigma);
    }

    public NormalDistributionGenerator() {
        super ((rows, columns) -> (float) ThreadLocalRandom.current().nextGaussian());
    }
}
