package ru.sashasuper.logic;

import ru.sashasuper.logic.functions.ActivateFunction;

import java.io.Serializable;

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
}
