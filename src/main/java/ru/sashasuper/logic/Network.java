package ru.sashasuper.logic;

import java.io.Serializable;

public class Network implements Serializable {
    private int inputSize;
    private int outputSize;
    private int layerCount;
    private Matrix[] weightMatrices;

    public Network(int inputSize, int outputSize, int layerCount, Matrix[] weightMatrices) {
        this.inputSize = inputSize;
        this.outputSize = outputSize;
        this.layerCount = layerCount;
        this.weightMatrices = weightMatrices;
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
}
