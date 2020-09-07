package ru.sashasuper.logic.functions;

public class Identity implements ElementActivateFunction {

    @Override
    public float derivative(float value) {
        return 1;
    }

    @Override
    public float process(float value) {
        return value;
    }
}
