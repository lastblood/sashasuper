package ru.sashasuper.logic.neural;

import ru.sashasuper.logic.Matrix;
import ru.sashasuper.logic.Network;
import ru.sashasuper.logic.Vector;

import static ru.sashasuper.logic.VectorMath.*;
import static ru.sashasuper.utils.Assertions.thr;

public class Neural {
    public static Vector process(Network network, Vector input) {
        Vector[] vectors = processUniversally(network, input, false);
        thr(vectors.length != 1 || vectors[0] == null);
        return vectors[0];
    }

    private static Vector[] processUniversally(Network network, Vector input, boolean rememberSteps) {
        thr(network.getWeightMatrices()[0].getColumns() != input.getLen(),
                "input.length != matrix[0].columns");

        Vector[] memories = new Vector[network.getLayerCount() + 1];
        memories[0] = input;

        Vector currentVector = input;
        for (int i = 0; i < network.getWeightMatrices().length; i++) {
            currentVector = multMatrixVector(network.getWeightMatrices()[i], currentVector);
            currentVector = applyToVector(currentVector, network.getActivateFunction());
            if (rememberSteps) memories[i + 1] = currentVector;
        }

        return rememberSteps ? memories : new Vector[]{currentVector};
    }

    public static Neural backPropagation(Network network, Vector input, Vector expectedOutput) {
        // Прогнать прямое распространение и запомнить результаты
        Vector[] vectors = processUniversally(network, input, true);
        thr(vectors.length != network.getLayerCount() + 1);
        int currentIndex = network.getLayerCount();

        // Найти ошибку для выходного вектора
        Vector error_layer = subElements(vectors[network.getLayerCount()], expectedOutput);

        // Прогнать через backPropagation все слои
        for (int index = currentIndex; index > 0; index--) {
            error_layer = backPropagationIter(network, vectors, error_layer, index);
        }

        return null; //todo
    }

    private static Vector backPropagationIter(Network network, Vector[] networkState, Vector error_layer, int currentIndex) {
        thr(currentIndex == 0 || currentIndex >= networkState.length);

        Vector currentLayer = networkState[currentIndex];
        Vector lastLayer = networkState[currentIndex - 1];

        // Найти вектор градиентов
        Vector gradient_layer = applyToVector(currentLayer, network.getActivateFunction(), true);

        // Вектор дельты
        Vector delta_layer = multElements(error_layer, gradient_layer);

        // Корректировочная матрица для текущей матрицы весов в Network
        Matrix subMatrix = multMatrixByT(multVectors(delta_layer, lastLayer), network.getLearningRate());

        thr(subMatrix.getColumns() != network.getWeightMatrices()[currentIndex].getColumns() ||
                subMatrix.getRows() != network.getWeightMatrices()[currentIndex].getRows());

        // Корректируем текущую матрицу весов
        network.getWeightMatrices()[currentIndex] = subMatrices(network.getWeightMatrices()[currentIndex], subMatrix);

        // Отправляем ошибку на уровень назад
        return multMatrixVectorTransposed(network.getWeightMatrices()[currentIndex], delta_layer);
    }
}
