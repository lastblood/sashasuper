package ru.sashasuper.logic;

import ru.sashasuper.io.Dataset;
import ru.sashasuper.logic.functions.ActivateFunction;
import ru.sashasuper.preprocessing.ElasticDeformation;
import ru.sashasuper.utils.NanDefender;

import java.awt.*;
import java.io.Serializable;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.stream.IntStream;

import static ru.sashasuper.logic.VectorMath.*;
import static ru.sashasuper.utils.Assertions.thr;

public class Network implements Serializable, Cloneable {
    private int inputSize;
    private int outputSize;
    private int layerCount;

    private Matrix[] weightMatrices;
    private ActivateFunction activateFunction;
    private float learningRate;
    private boolean withBias;

    public float regularizationRate = 0.03f;

    public Network(Matrix[] weightMatrices, ActivateFunction activateFunction, float learningRate, boolean withBias) {
        thr(weightMatrices == null);
        thr(learningRate <= 0 || learningRate > 1);

        this.inputSize = weightMatrices[0].getColumns();
        this.outputSize = weightMatrices[weightMatrices.length - 1].getRows();
        this.layerCount = weightMatrices.length - 1;

        this.weightMatrices = weightMatrices;
        this.activateFunction = activateFunction;
        this.learningRate = learningRate;
        this.withBias = withBias;
    }

    public Network(Matrix[] weightMatrices, ActivateFunction activateFunction, float learningRate) {
        thr(weightMatrices == null);
        thr(learningRate <= 0 || learningRate > 1);

        this.inputSize = weightMatrices[0].getColumns();
        this.outputSize = weightMatrices[weightMatrices.length - 1].getRows();
        this.layerCount = weightMatrices.length - 1;

        this.weightMatrices = weightMatrices;
        this.activateFunction = activateFunction;
        this.learningRate = learningRate;

        this.withBias = true;
    }

    public int getInputSize() {
        return inputSize;
    }

    public int getOutputSize() {
        return outputSize;
    }

    public int getHiddenLayerCount() {
        return layerCount;
    }

    public Matrix[] getWeightMatrices() {
        return weightMatrices;
    }

    public ActivateFunction getActivateFunction() {
        return activateFunction;
    }

    public float getLearningRate() {
        return learningRate;
    }

    public void setLearningRate(float learningRate) {
        this.learningRate = learningRate;
    }

    public int getLayerCount() {
        return layerCount;
    }

    public boolean isWithBias() {
        return withBias;
    }

    // Всегда воспринимает, как non-biased input
    public Vector process(float ... values) {
        return process(new Vector(values));
    }

    public Vector process(boolean biased, float ... values) {
        return process(new Vector(biased, values));
    }

    public Vector process(Vector input) {
        thr(getWeightMatrices()[0].getColumns() != input.getLength(withBias));

        Vector currentVector = input;
        for (int i = 0; i < getWeightMatrices().length; i++) {
            currentVector = multMatrixVector(getWeightMatrices()[i], currentVector, withBias);
            currentVector = applyToVector(currentVector, getActivateFunction());
        }

        return currentVector;
    }

    static final Dimension MNIST_DIMENSION = new Dimension(28, 28);
    ElasticDeformation deformator = new ElasticDeformation(5, 0.05f, new Random(123456));

    public void trainAtBatch(Dataset data) {
        List<SimpleEntry<Vector, Vector>> all = data.getAll();
        Matrix[] subMatrices = all.parallelStream()
                .map(x -> backPropagation(deformator.deformate(x.getKey(), MNIST_DIMENSION), x.getValue(), false))
                .reduce((matrices1, matrices2) -> IntStream.range(0, matrices1.length)
                        .mapToObj(index -> addMatrices(matrices1[index], matrices2[index]))
                        .toArray(Matrix[]::new))
                .orElseThrow();

        for (int i = 0; i < getWeightMatrices().length; i++) {
            Matrix weightMatrix = getWeightMatrices()[i];
            Matrix subMatrix = subMatrices[i];
            thr(weightMatrix.getRows() != subMatrix.getRows() ||
                    weightMatrix.getColumns() != subMatrix.getColumns());

            float[][] matrixValues = weightMatrix.getValues();
            float[][] subMatrixValues = subMatrix.getValues();

            for (int y = 0; y < weightMatrix.getRows(); y++) {
                for (int x = 0; x < weightMatrix.getColumns(); x++) {
                    matrixValues[y][x] = matrixValues[y][x] * (1 - learningRate*regularizationRate/60000)
                            - (learningRate / all.size()) * subMatrixValues[y][x];
                }
            }
        }
    }

    // Используется только вычитание двух векторов (И ТОЛЬКО ЗДЕСЬ)
    public Matrix[] backPropagation(Vector input, Vector expectedOutput, boolean correct) {
        thr(getWeightMatrices()[0].getColumns() != input.getLength(withBias));

        Vector[] activations = new Vector[getHiddenLayerCount() + 2];
        activations[0] = input;

        Vector[] z_vectors = new Vector[getHiddenLayerCount() + 1];

        for (int i = 0; i < getWeightMatrices().length; i++) {
            z_vectors[i] = multMatrixVector(getWeightMatrices()[i], activations[i], withBias);
            activations[i + 1] = applyToVector(z_vectors[i], getActivateFunction());
        }

//        thr(!Arrays.stream(vectors).allMatch(x -> x.getValues()[x.getValues().length - 1] == 1));

            // Найти ошибку для выходного вектора
        Vector costLayer = subElements(activations[getHiddenLayerCount() + 1], expectedOutput, withBias);
//        Vector gradientLayer = applyToVector(z_vectors[z_vectors.length-1], activateFunction, true);
//        Vector errorLayer = multElements(costLayer, gradientLayer, withBias);
        Vector errorLayer = costLayer; // Cross-entropy loss trigger

        thr(NanDefender.inVector(errorLayer));
//        thr(!Arrays.stream(activations).allMatch(x -> x.getValues()[x.getValues().length - 1] == 1));

        Matrix[] subMatrices = new Matrix[getWeightMatrices().length];

            // Прогнать через backPropagation все слои
        for (int index = getWeightMatrices().length - 1; index >= 0; index--) {
            thr(!Arrays.stream(activations).allMatch(x -> x.getValues()[x.getValues().length - 1] == 1));
            errorLayer = backPropagationIter(activations[index], z_vectors[index],
                            errorLayer, subMatrices, index, correct);
            thr(NanDefender.inVector(errorLayer));
        }

        return subMatrices;
    }

    // Перемножение вектора-строки и вектора-столбца, транспонирование матрицы, умножение матрицы на число,
    //  поэлементное вычитание матриц, применение матрицы к вектору с другой стороны (ВСЕ ЭТО ТОЛЬКО ЗДЕСЬ)
    // ATTENTION!!! НЕ ПОТОКО-БЕЗОПАСНОЕ, МЕНЯЕТ МАТРИЦЫ ВНУТРИ network
    // currentIndex указывает на индекс текущей меняемой матрицы

    public Matrix[] backPropagation(Vector input, Vector expectedOutput) {
        return backPropagation(input, expectedOutput, true);
    }
    private Vector backPropagationIter(Vector lastLayer, Vector currentLayer,
                            Vector nextError, Matrix[] subMatrices, int currentIndex, boolean correct) {
        thr(currentIndex < 0 || currentIndex >= weightMatrices.length);

        Vector gradient_layer = applyToVector(currentLayer, activateFunction, true);
//        thr(NanDefender.inVector(gradient_layer));

        // Вектор дельты
        Vector delta_layer = multElements(nextError, gradient_layer, withBias);
//        thr(NanDefender.inVector(delta_layer));

        // Корректировочная матрица для текущей матрицы весов в Network
        subMatrices[currentIndex] = multMatrixByT(
                multVectors(lastLayer, delta_layer, withBias), getLearningRate());
//        thr(NanDefender.inMatrix(subMatrix));

        Matrix current = getWeightMatrices()[currentIndex];
//        thr(NanDefender.inMatrix(current));
        thr(subMatrices[currentIndex].getColumns() != current.getColumns() ||
                subMatrices[currentIndex].getRows() != current.getRows());

        Vector result = multMatrixVectorTransposed(getWeightMatrices()[currentIndex], delta_layer, withBias);
        if(correct) { // Корректируем текущую матрицу весов
            for (int y = 0; y < current.getRows(); y++)
                for (int x = 0; x < current.getColumns() - 1; x++)
                    current.getValues()[y][x] *= 1 - learningRate*regularizationRate/60000;

            getWeightMatrices()[currentIndex] = subMatrices(current, subMatrices[currentIndex]);
        }

        // Возвращаем ошибку, которую надо будет отправить на уровень назад
        return result;
    }

    private static int getMaxIndex(Vector v) {
        int max = 0;
        for (int i = 1; i < v.getValues().length - 1; i++) {
            if(v.getValues()[i] > v.getValues()[max]) {
                max = i;
            }
        }
        return max;
    }

    public MomentumStat test(Dataset dataset) {
        return test(dataset.getAll());
    }

    public MomentumStat test(List<SimpleEntry<Vector, Vector>> entriesList) {
        float min = Float.MAX_VALUE, max = Float.MIN_VALUE;
        double resultError = 0.0;
        int right = 0, wrong = 0;

        List<SimpleEntry<Vector, Vector>> dataList = new ArrayList<>(entriesList);
        Collections.shuffle(dataList);

//        List<AbstractMap.SimpleEntry<Vector, Vector>> bad = new ArrayList<>(dataList.size() / 10);

        for (SimpleEntry<Vector, Vector> entry : dataList) {
            Vector resultVector = process(entry.getKey());

            int ind1 = getMaxIndex(entry.getValue()), ind2 = getMaxIndex(resultVector);
            thr(ind1 < 0 || ind1 > 9 || ind2 < 0 || ind2 > 9);

            float mse = VectorMath.MSE(resultVector, entry.getValue());

            if(ind1 == ind2)
                right++;
            else
                wrong++;

//            if(ind1 != ind2 || mse > 0.7f)
//                bad.add(entry);

            min = Float.min(mse, min);
            max = Float.max(mse, max);

            resultError += mse;
        }

        MomentumStat momentumStat = new MomentumStat(right, wrong, min, max, resultError);
//        Collections.shuffle(bad);
//        momentumStat.bad = bad;
        return momentumStat;
    }

    public void learnIteration(Dataset dataset) {
        for (SimpleEntry<Vector, Vector> entry : dataset.getAll()) {
            backPropagation(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Network c = (Network) super.clone();

        Matrix[] matrices = new Matrix[weightMatrices.length];
        for (int i = 0; i < matrices.length; i++) {
            matrices[i] = (Matrix) getWeightMatrices()[i].clone();
        }
        c.weightMatrices = matrices;

        return c;
    }
}
