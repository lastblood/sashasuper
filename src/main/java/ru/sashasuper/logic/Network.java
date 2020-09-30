package ru.sashasuper.logic;

import ru.sashasuper.io.Dataset;
import ru.sashasuper.logic.functions.ActivateFunction;
import ru.sashasuper.logic.functions.Logistic;
import ru.sashasuper.utils.MomentumStat;
import ru.sashasuper.utils.NanDefender;

import java.io.Serializable;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ru.sashasuper.logic.VectorMath.*;
import static ru.sashasuper.utils.Assertions.thr;

public class Network implements Serializable, Cloneable {
    private int inputSize;
    private int outputSize;
    private int layerCount;

    private int epochSize = 60000;

    private Matrix[] weightMatrices;
    private boolean withBias;
    private ActivateFunction activateFunction;
    private LOSS_FUNCTION lossFunction;

    public static final long serialVersionUID = 8143914185496047632L;

    private float learningRate;
    private float regularizationRate = 0f;

    public enum LOSS_FUNCTION {
        MEAN_SQUARED_ERROR, CROSS_ENTROPY
    }

    public Network(Matrix[] weightMatrices, ActivateFunction activateFunction, float learningRate,
                    float regularizationRate, boolean withBias, LOSS_FUNCTION lossFunction, int epochSize) {
        thr(weightMatrices == null);
        thr(learningRate <= 0);

        this.inputSize = weightMatrices[0].getColumns();
        this.outputSize = weightMatrices[weightMatrices.length - 1].getRows();
        this.layerCount = weightMatrices.length - 1;

        this.weightMatrices = weightMatrices;
        this.activateFunction = activateFunction;
        this.learningRate = learningRate;
        this.withBias = withBias;
        this.regularizationRate = regularizationRate;
        this.lossFunction = lossFunction;
        this.epochSize = epochSize;
    }

    public Network(Matrix[] weightMatrices, ActivateFunction activateFunction, float learningRate, boolean withBias) {
        thr(weightMatrices == null);
        thr(learningRate <= 0);

        this.inputSize = weightMatrices[0].getColumns();
        this.outputSize = weightMatrices[weightMatrices.length - 1].getRows();
        this.layerCount = weightMatrices.length - 1;

        this.weightMatrices = weightMatrices;
        this.activateFunction = activateFunction;
        this.learningRate = learningRate;
        this.withBias = withBias;
        this.lossFunction = LOSS_FUNCTION.MEAN_SQUARED_ERROR;
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

    private Matrix[] getWeightMatrices() {
        return weightMatrices;
    }

    public ActivateFunction getActivateFunction() {
        return activateFunction;
    }

    public float getLearningRate() {
        return learningRate;
    }

    public void setLearningRate(float learningRate) {
        if(learningRate > 0) this.learningRate = learningRate;
    }

    public float getRegularizationRate() {
        return regularizationRate;
    }

    public void setRegularizationRate(float regularizationRate) {
        this.regularizationRate = regularizationRate;
    }

    public int getLayerCount() {
        return layerCount;
    }

    public LOSS_FUNCTION getLossFunction() {
        return lossFunction;
    }

    public boolean isWithBias() {
        return withBias;
    }

    public void setEpochSize(int epochSize) {
        this.epochSize = epochSize;
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
            currentVector = getActivateFunction().process(currentVector);
        }

        return currentVector;
    }

    public void trainAtBatch(Dataset data) {
        List<SimpleEntry<Vector, Vector>> all = data.getAll();
        Matrix[] subMatrices = all.parallelStream()
                .map(x -> backPropagation(
                                x.getKey(),
                        x.getValue(), false))
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
                    matrixValues[y][x] = matrixValues[y][x] * (1 - learningRate*regularizationRate/epochSize)
                            - (learningRate / all.size()) * subMatrixValues[y][x];
                }
            }
        }
    }

    // Суммирует матрицы одинакового размера в одну
    private static Matrix sumFrom(Matrix ... matrices) {
        // Работает только для матриц одинаковых размеров
        thr(Arrays.stream(matrices).anyMatch(m ->
                matrices[0].getColumns() != m.getColumns() || matrices[0].getRows() != m.getRows()));
        int rows = matrices[0].getRows();
        int columns = matrices[0].getColumns();
        float[][] result = new float[rows][columns];
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                for (Matrix matrix : matrices)
                    result[y][x] += matrix.getValues()[y][x];
            }
        }
        return new Matrix(result);
    }



    public Matrix[] backPropagation(Vector input, Vector expectedOutput) {
        return backPropagation(input, expectedOutput, true);
    }

    public Matrix[] backPropagation(Vector input, Vector expectedOutput, boolean correct) {
        thr(getWeightMatrices()[0].getColumns() != input.getLength(withBias));

        Vector[] activations = new Vector[getHiddenLayerCount() + 2];
        activations[0] = input;
        Vector[] z_vectors = new Vector[getHiddenLayerCount() + 1];

        for (int i = 0; i < getWeightMatrices().length; i++) {
            z_vectors[i] = multMatrixVector(getWeightMatrices()[i], activations[i], withBias);
            activations[i + 1] = getActivateFunction().process(z_vectors[i]);
        }

        // Найти ошибку для выходного вектора
        Vector costLayer = subElements(activations[getHiddenLayerCount() + 1], expectedOutput, withBias);

        Vector errorLayer = null;
        if(lossFunction == LOSS_FUNCTION.MEAN_SQUARED_ERROR) {
            Vector gradientLayer = getActivateFunction().derivative(z_vectors[z_vectors.length - 1]);
            errorLayer = multElements(costLayer, gradientLayer, withBias);
        } else if(lossFunction == LOSS_FUNCTION.CROSS_ENTROPY) {
            // Использование только для поддерживающих подобное сокращение функций потерь
            errorLayer = costLayer;
        }

        thr(errorLayer == null || NanDefender.inVector(errorLayer));
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


    // ATTENTION!!! НЕ ПОТОКО-БЕЗОПАСНОЕ, МЕНЯЕТ МАТРИЦЫ ВНУТРИ network
    // currentIndex указывает на индекс текущей меняемой матрицы
    private Vector backPropagationIter(Vector lastLayer, Vector currentLayer,
                            Vector nextError, Matrix[] subMatrices, int currentIndex, boolean correct) {
        thr(currentIndex < 0 || currentIndex >= weightMatrices.length);

        Vector gradient_layer = getActivateFunction().derivative(currentLayer);
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
                    current.getValues()[y][x] *= 1 - learningRate*regularizationRate/epochSize;

            getWeightMatrices()[currentIndex] = subMatrices(current, subMatrices[currentIndex]);
        }

        // Возвращаем ошибку, которую надо будет отправить на уровень назад
        return result;
    }

    public static int getMaxIndex(Vector v) {
        int max = 0;
        for (int i = 1; i < v.getValues().length - 1; i++) {
            if(v.getValues()[i] > v.getValues()[max]) {
                max = i;
            }
        }
        return max;
    }

    public static boolean isRightClassificated(SimpleEntry<Vector, Vector> entry) {
        return getMaxIndex(entry.getKey()) == getMaxIndex(entry.getValue());
    }

    // Многопоточная версия тестирования через стандартный пул для parallelStream
    public MomentumStat mtTest(Dataset dataset) { return mtTest(dataset.getAll()); }

    public MomentumStat mtTest(List<SimpleEntry<Vector, Vector>> entriesList) {
        List<SimpleEntry<Vector, Vector>> ans =
                entriesList.parallelStream()
                        .map(x -> new SimpleEntry<>(x.getValue(), process(x.getKey())))
                        .collect(Collectors.toList());

        int right = (int) ans.stream().filter(Network::isRightClassificated).count(), wrong = ans.size() - right;
        double sumMSE = 0.0, sumCE = 0.0;

        for (SimpleEntry<Vector, Vector> entry : ans) {
            sumMSE += VectorMath.MSE(entry.getKey(), entry.getValue());
            sumCE += VectorMath.CE(entry.getKey(), entry.getValue());
        }
        return new MomentumStat(right, wrong, sumMSE, sumCE);
    }


    public MomentumStat test(Dataset dataset) {
        return test(dataset.getAll());
    }

    public MomentumStat test(List<SimpleEntry<Vector, Vector>> entriesList) {
        float min = Float.MAX_VALUE, max = Float.MIN_VALUE;
        double sumMSE = 0.0, sumCE = 0.0;
        int right = 0, wrong = 0;

        List<SimpleEntry<Vector, Vector>> dataList = new ArrayList<>(entriesList);

        for (SimpleEntry<Vector, Vector> entry : dataList) {
            Vector resultVector = process(entry.getKey());

            int ind1 = getMaxIndex(entry.getValue()), ind2 = getMaxIndex(resultVector);
            if(ind1 == ind2)
                right++;
            else
                wrong++;

            sumMSE += VectorMath.MSE(entry.getValue(), resultVector);
            sumCE += VectorMath.CE(entry.getValue(), resultVector);
        }

        return new MomentumStat(right, wrong, sumMSE, sumCE);
    }

    // Одна эпоха стохастического градиентного спуска
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

    public static class NetworkBuilder {
        private ActivateFunction activation = new Logistic();
        private float learningRate = 0.1f;
        private float regularizationRate = 0.0f;
        private boolean biased = true;
        private LOSS_FUNCTION lossFunction = LOSS_FUNCTION.MEAN_SQUARED_ERROR;
        private int epochSize = 60000;
        private Matrix[] matrices = null;

        public NetworkBuilder activation(ActivateFunction activation) {
            this.activation = activation;
            return this;
        }

        public NetworkBuilder learningRate(float learningRate) {
            this.learningRate = learningRate;
            return this;
        }

        public NetworkBuilder regularizationRate(float regularizationRate) {
            this.regularizationRate = regularizationRate;
            return this;
        }

        public NetworkBuilder biased(boolean biased) {
            this.biased = biased;
            return this;
        }

        public NetworkBuilder lossFunction(LOSS_FUNCTION lossFunction) {
            this.lossFunction = lossFunction;
            return this;
        }

        public NetworkBuilder matrices(Matrix[] matrices) {
            this.matrices = matrices;
            return this;
        }

        public NetworkBuilder epochSize(int epochSize) {
            this.epochSize = epochSize;
            return this;
        }

        public Network build() {
            thr(matrices == null, "You should set matrices in builder");
            return new Network(matrices, activation, learningRate,
                    regularizationRate, biased, lossFunction, epochSize);
        }
    }

    public static NetworkBuilder builder() {
        return new NetworkBuilder();
    }
}
