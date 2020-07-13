package ru.sashasuper.logic.functions;

public class SoftPlus extends ActivateFunction {

    @Override
    public float derivative(float value) {
        return (float) (1 / (1 + Math.pow(Math.E, -value)));
    }

    @Override
    public float process(float value) {
        return (float) Math.log(1 + Math.pow(Math.E, value));
    }
}
