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

    public Network() {
    }

    public int getInputSize() {
        return inputSize;
    }

    public void setInputSize(int inputSize) {
        this.inputSize = inputSize;
    }

    public int getOutputSize() {
        return outputSize;
    }

    public void setOutputSize(int outputSize) {
        this.outputSize = outputSize;
    }

    public int getLayerCount() {
        return layerCount;
    }

    public void setLayerCount(int layerCount) {
        this.layerCount = layerCount;
    }

    public Matrix[] getWeightMatrices() {
        return weightMatrices;
    }

    public void setWeightMatrices(Matrix[] weightMatrices) {
        this.weightMatrices = weightMatrices;
    }
}
