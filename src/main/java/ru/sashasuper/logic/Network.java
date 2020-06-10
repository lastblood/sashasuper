package ru.sashasuper.logic;

import ru.sashasuper.logic.functions.ActivateFunction;

import java.io.Serializable;

public class Network implements Serializable {
    private int inputSize;
    private int outputSize;
    private int layerCount;
    private Matrix[] weightMatrices;
    private ActivateFunction activateFunction;
    private float learningRate;

    public Network(int inputSize, int outputSize, int layerCount, Matrix[] weightMatrices,
                   ActivateFunction activateFunction, float learningRate) {

        this.inputSize = inputSize;
        this.outputSize = outputSize;
        this.layerCount = layerCount;
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

    public int getLayerCount() {
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
