package ru.sashasuper.logic;

import ru.sashasuper.logic.functions.ActivateFunction;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Arrays;

import static ru.sashasuper.logic.VectorMath.*;
import static ru.sashasuper.utils.Assertions.thr;

public class Network implements Serializable {
    private int inputSize;
    private int outputSize;
    private int layerCount;

    private Matrix[] weightMatrices;
    private ActivateFunction activateFunction;
    private float learningRate;
    private boolean withBias;

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

    // todo: убрать этот позор
    public Network(Matrix[] weightMatrices, ActivateFunction activateFunction, float learningRate) {
        thr(weightMatrices == null);
        thr(learningRate <= 0 || learningRate > 1);

        this.inputSize = weightMatrices[0].getColumns();
        this.outputSize = weightMatrices[weightMatrices.length - 1].getRows();
        this.layerCount = weightMatrices.length - 1;

        this.weightMatrices = weightMatrices;
        this.activateFunction = activateFunction;
        this.learningRate = learningRate;

        this.withBias = false;
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

    public Vector process(Vector input) {
        Vector[] vectors = processUniversally(input, false);
        thr(vectors.length != 1 || vectors[0] == null);
        thr(getWeightMatrices()[0].getColumns() != input.getLength(withBias));
        return vectors[0];
    }

    // Всегда воспринимает, как non-biased input
    public Vector process(float ... values) {
        return process(new Vector(values));
    }

    public Vector process(boolean biased, float ... values) {
        return process(new Vector(biased, values));
    }

    // Применение матрицы к вектору (ТОЛЬКО ЗДЕСЬ), применение функции к вектору
    // Почему бы и не объединить обе эти операции? Проще будет реализовать исполнение на видеокартах
    // Умеет запоминать состояние промежуточных векторов внутри сети для использования в backpropagation
    public Vector[] processUniversally(Vector input, boolean rememberSteps) {
        thr(getWeightMatrices()[0].getColumns() != input.getLength(withBias));

        Vector[] memories = new Vector[getHiddenLayerCount() + 2];
        memories[0] = input;

        Vector currentVector = input;
        for (int i = 0; i < getWeightMatrices().length; i++) {
            // todo: оптимизировать матричные операции для создания вектора сразу с bias
            currentVector = multMatrixVector(getWeightMatrices()[i], currentVector, withBias);
            currentVector = applyToVector(currentVector, getActivateFunction());
            if (rememberSteps) memories[i + 1] = currentVector;
        }

        return rememberSteps ? memories : new Vector[]{currentVector};
    }

    // Используется только вычитание двух векторов (И ТОЛЬКО ЗДЕСЬ)
    public void backPropagation(Vector input, Vector expectedOutput) {
        // Прогнать прямое распространение и запомнить результаты
        Vector[] vectors = processUniversally(input, true);
        thr(!Arrays.stream(vectors).allMatch(x -> x.getValues()[x.getValues().length - 1] == 1));

        thr(vectors.length != getHiddenLayerCount() + 2);
//        System.out.println("vectors = " + Arrays.toString(vectors));

        // Найти ошибку для выходного вектора
        Vector error_layer = subElements(vectors[getHiddenLayerCount() + 1], expectedOutput, withBias);

        thr(!Arrays.stream(vectors).allMatch(x -> x.getValues()[x.getValues().length - 1] == 1));

        // Прогнать через backPropagation все слои
        for (int index = getWeightMatrices().length - 1; index >= 0; index--) {
            thr(!Arrays.stream(vectors).allMatch(x -> x.getValues()[x.getValues().length - 1] == 1));
            error_layer = backPropagationIter(vectors, error_layer, index);
        }
    }

    // Перемножение вектора-строки и вектора-столбца, транспонирование матрицы, умножение матрицы на число,
    //  поэлементное вычитание матриц, применение матрицы к вектору с другой стороны (ВСЕ ЭТО ТОЛЬКО ЗДЕСЬ)
    
    // ATTENTION!!! НЕ ПОТОКО-БЕЗОПАСНОЕ, МЕНЯЕТ МАТРИЦЫ ВНУТРИ network
    // currentIndex указывает на индекс текущей меняемой матрицы
    private Vector backPropagationIter(Vector[] networkState, Vector error_layer, int currentIndex) {
//        System.out.println("backPropagationIter():\nerror_layer = " + error_layer);
        thr(currentIndex < 0 || currentIndex >= weightMatrices.length);

        Vector currentLayer = networkState[currentIndex + 1];
        Vector lastLayer = networkState[currentIndex];

        Vector gradient_layer = applyToVector(currentLayer, activateFunction, true);
        // Очевидно, для identity gradient_layer всегда будет равен единице

        // Вектор дельты
        Vector delta_layer = multElements(error_layer, gradient_layer, withBias);
//        System.out.println("delta_layer = " + delta_layer);

        //todo: можно реализовать смешанную операцию: умножение двух векторов

//БЫЛО
//        Matrix temp = multVectors(delta_layer, lastLayer, withBias);
        Matrix temp = multVectors(lastLayer, delta_layer, withBias);
//        System.out.println("temp = " + temp);

        // Корректировочная матрица для текущей матрицы весов в Network
        Matrix subMatrix = multMatrixByT(temp, getLearningRate());
//        System.out.println("subMatrix = " + subMatrix);

        Matrix current = getWeightMatrices()[currentIndex];
//        System.out.println("current = " + current);
        thr(subMatrix.getColumns() != current.getColumns() || subMatrix.getRows() != current.getRows());

        // Корректируем текущую матрицу весов
        getWeightMatrices()[currentIndex] = subMatrices(current, subMatrix);

        // Отправляем ошибку на уровень назад
        return multMatrixVectorTransposed(getWeightMatrices()[currentIndex], delta_layer, withBias);
    }

    public double test(Dataset dataset) {
        double resultError = 0.0;
        for (AbstractMap.SimpleEntry<Vector, Vector> entry : dataset.getAll()) {
            Vector resultVector = process(entry.getKey());
            float mse = VectorMath.MSE(resultVector, entry.getValue());
            resultError += mse;
        }
        return resultError;
    }

    public void learnIteration(Dataset dataset) {
        for (AbstractMap.SimpleEntry<Vector, Vector> entry : dataset.getAll()) {
            backPropagation(entry.getKey(), entry.getValue());
        }
    }
}
