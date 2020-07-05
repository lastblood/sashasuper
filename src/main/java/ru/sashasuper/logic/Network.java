package ru.sashasuper.logic;

import ru.sashasuper.logic.functions.ActivateFunction;

import java.io.Serializable;
import java.util.AbstractMap;

import static ru.sashasuper.logic.VectorMath.*;
import static ru.sashasuper.utils.Assertions.thr;

public class Network implements Serializable {
    private int inputSize;
    private int outputSize;
    private int layerCount;
    private Matrix[] weightMatrices;
    private ActivateFunction activateFunction;
    private float learningRate;

    public Network(int inputSize, int outputSize, int hiddenLayerCount, Matrix[] weightMatrices,
                   ActivateFunction activateFunction, float learningRate) {

        thr(weightMatrices == null || weightMatrices.length != hiddenLayerCount + 1);
        thr(learningRate <= 0 || learningRate > 1);

        thr(weightMatrices[0].getColumns() != inputSize);
        thr(weightMatrices[hiddenLayerCount].getRows() != outputSize);

        this.inputSize = inputSize;
        this.outputSize = outputSize;
        this.layerCount = hiddenLayerCount;
        this.weightMatrices = weightMatrices;
        this.activateFunction = activateFunction;
        this.learningRate = learningRate;
    }

    private Network() {
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


    public Vector process(Vector input) {
        Vector[] vectors = processUniversally(input, false);
        thr(vectors.length != 1 || vectors[0] == null);
        return vectors[0];
    }

    public Vector process(float ... values) {
        thr(getWeightMatrices()[0].getColumns() != values.length,
                "values.length != matrix[0].columns");
        return process(new Vector(values));
    }

    // Умеет запоминать состояние промежуточных векторов внутри сети для использования в backpropagation
    public Vector[] processUniversally(Vector input, boolean rememberSteps) {
        thr(getWeightMatrices()[0].getColumns() != input.getLen(),
                "input.length != matrix[0].columns");

        Vector[] memories = new Vector[getHiddenLayerCount() + 2];
        memories[0] = input;
//        System.out.println("input " + input);

        Vector currentVector = input;
        for (int i = 0; i < getWeightMatrices().length; i++) {
            currentVector = multMatrixVector(getWeightMatrices()[i], currentVector);
//            System.out.println("after mult " + currentVector);
            currentVector = applyToVector(currentVector, getActivateFunction());
//            System.out.println("after activation " + currentVector);
            if (rememberSteps) memories[i + 1] = currentVector;
        }

        return rememberSteps ? memories : new Vector[]{currentVector};
    }

    public void backPropagation(Vector input, Vector expectedOutput) {
        // Прогнать прямое распространение и запомнить результаты
        Vector[] vectors = processUniversally(input, true);
        thr(vectors.length != getHiddenLayerCount() + 2);

        // Найти ошибку для выходного вектора
        Vector error_layer = subElements(vectors[getHiddenLayerCount() + 1], expectedOutput);
//        System.out.println("end_error = " + error_layer);

        // Прогнать через backPropagation все слои
        for (int index = getWeightMatrices().length - 1; index >= 0; index--) {
            error_layer = backPropagationIter(vectors, error_layer, index);
        }
    }

    // ATTENTION!!! НЕ ПОТОКО-БЕЗОПАСНОЕ, МЕНЯЕТ МАТРИЦЫ ВНУТРИ network
    // currentIndex указывает на индекс текущей меняемой матрицы
    private Vector backPropagationIter(Vector[] networkState, Vector error_layer, int currentIndex) {
        thr(currentIndex < 0 || currentIndex >= weightMatrices.length);

        Vector currentLayer = networkState[currentIndex + 1];
        Vector lastLayer = networkState[currentIndex];

        // Найти вектор градиентов
        Vector gradient_layer = applyToVector(currentLayer, activateFunction, true);

        // Вектор дельты
        Vector delta_layer = multElements(error_layer, gradient_layer);

//БЫЛО        Matrix temp = multVectors(delta_layer, lastLayer);
        Matrix temp = multVectors(lastLayer, delta_layer);

        // Корректировочная матрица для текущей матрицы весов в Network
        Matrix subMatrix = multMatrixByT(temp, getLearningRate());
//        System.out.println("submatrix: " + subMatrix.toString());

        Matrix current = getWeightMatrices()[currentIndex];
        thr(subMatrix.getColumns() != current.getColumns() || subMatrix.getRows() != current.getRows());

        // Корректируем текущую матрицу весов
        getWeightMatrices()[currentIndex] = subMatrices(current, subMatrix);

        // Отправляем ошибку на уровень назад
        return multMatrixVectorTransposed(getWeightMatrices()[currentIndex], delta_layer);
    }

    public double test(Dataset dataset) {
        double resultError = 0.0;
        for (AbstractMap.SimpleEntry<Vector, Vector> entry : dataset.getAll()) {
            Vector resultVector = process(entry.getKey());
//            System.out.println(Arrays.toString(entry.getKey().getValues()) + " " + Arrays.toString(resultVector.getValues()));
//            System.out.println("resultVector.getValues() = " + Arrays.toString(resultVector.getValues()));
            float mse = VectorMath.MSE(resultVector, entry.getValue());
//            System.out.println(mse);
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
