package ru.sashasuper.logic.functions;

public class SoftPlus implements ElementActivateFunction {

    @Override
    public float derivative(float value) {
        if(value > 10) return 1;
        return (float) (1 / (1 + Math.exp(-value)));
    }

    @Override
    public float process(float value) {
        if(value > 10) return value;
        return (float) Math.log(1 + Math.exp(value));
    }
}
