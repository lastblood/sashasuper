package ru.sashasuper.logic.functions;

public class Identity implements ActivateFunction {

    public float derivative(float value) {
        return 1;
    }

    public float process(float value) {
        return value;
    }
}
