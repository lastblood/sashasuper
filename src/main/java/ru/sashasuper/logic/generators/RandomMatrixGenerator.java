package ru.sashasuper.logic.generators;

import ru.sashasuper.logic.Matrix;

import java.util.Random;
import java.util.function.BiFunction;

import static ru.sashasuper.utils.Assertions.thr;

public class RandomMatrixGenerator extends AbstractMatrixGenerator {
    private Random random;
    private LambdaAllMatrixGenerator lambdaGenerator;

    private float defaultMin = 0, defaultMax = 1;

    private final BiFunction<Integer, Integer, Matrix> randomInternalGenerator = (rows, columns) -> {
        if(rows < 0 || columns < 0)
            throw new IllegalArgumentException("Rows and columns should be > 0");

        float[][] values = new float[rows][columns];
        for(int y = 0; y < rows; y++)
            for(int x = 0; x < columns; x++)
                values[y][x] = random.nextFloat();

        return new Matrix(values);
    };

    public RandomMatrixGenerator() {
        this.random = new Random();
        lambdaGenerator = new LambdaAllMatrixGenerator(randomInternalGenerator);
    }

    public RandomMatrixGenerator(Random random) {
        this.random = random;
        lambdaGenerator = new LambdaAllMatrixGenerator(randomInternalGenerator);
    }

    public RandomMatrixGenerator(float defaultMin, float defaultMax) {
        this.random = new Random();
        this.defaultMin = defaultMin;
        this.defaultMax = defaultMax;
        lambdaGenerator = new LambdaAllMatrixGenerator(randomInternalGenerator);
    }

    public RandomMatrixGenerator(Random random, float defaultMin, float defaultMax) {
        this.random = random;
        this.defaultMin = defaultMin;
        this.defaultMax = defaultMax;
        lambdaGenerator = new LambdaAllMatrixGenerator(randomInternalGenerator);
    }


    // min included, max excluded
    // todo: После генерации производит "нормализацию" в границы нужного диапазона
    public Matrix generateMatrix(int rows, int columns, float min, float max) {
        Matrix result = generateMatrix(rows, columns);
        return (min == 0 && max == 1) ? result : normalizeMatrixTo(result, min, max);
    }

    public Matrix generateMatrix(int rows, int columns) {
        Matrix result = lambdaGenerator.generateMatrix(rows, columns);
        return (defaultMin == 0 && defaultMax == 1) ? result : normalizeMatrixTo(result, defaultMin, defaultMax);
    }

    @Override
    public Matrix[] generateMatrices(boolean biased, int... sizes) {
        return generateMatrices(biased, defaultMin, defaultMax, sizes);
    }

    // Генерация массива матриц для заданных размеров векторов (значащих полей, без biases)
    public Matrix[] generateMatrices(boolean biased, float min, float max, int ... sizes) {
        Matrix[] matrices = lambdaGenerator.generateMatrices(biased, sizes);

        if(min != 0 || max != 1)
            for (Matrix matrix : matrices)
                normalizeMatrixTo(matrix, min, max);

        return matrices;
    }

    // Изменяет оригинальный объект, ссылка возвращается на него же
    private Matrix normalizeMatrixTo(Matrix result, float min, float max) {
        thr(min > max);

        for (int i = 0; i < result.getRows(); i++) {
            for (int j = 0; j < result.getColumns(); j++) {
                result.getValues()[i][j] *= max - min;
                result.getValues()[i][j] += min;
            }
        }

        return result;
    }

    public RandomMatrixGenerator setDefaultMin(float defaultMin) {
        this.defaultMin = defaultMin;
        return this;
    }

    public RandomMatrixGenerator setDefaultMax(float defaultMax) {
        this.defaultMax = defaultMax;
        return this;
    }
}
