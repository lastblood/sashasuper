package ru.sashasuper.logic.functions;

public class Identity extends ActivateFunction {

    @Override
    public float derivative(float value) {
        return 1;
    }

    @Override
    public float process(float value) {
        return value;
    }
}
