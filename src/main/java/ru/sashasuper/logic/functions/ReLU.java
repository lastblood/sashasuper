package ru.sashasuper.logic.functions;

public class ReLU implements ElementActivateFunction {

    private float negativeMultiplier = 0.05f, positiveMultiplier = 1.0f;

    public ReLU() {}

    public ReLU(float negativeMultiplier, float positiveMultiplier) {
        this.negativeMultiplier = negativeMultiplier;
        this.positiveMultiplier = positiveMultiplier;
    }

    @Override
    public float process(float value) {
        return value * (value > 0 ? positiveMultiplier : negativeMultiplier);
    }

    @Override
    public float derivative(float value) {
        return value > 0 ? positiveMultiplier : negativeMultiplier;
    }
}
