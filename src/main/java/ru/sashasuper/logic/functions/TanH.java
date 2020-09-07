package ru.sashasuper.logic.functions;

public class TanH implements ElementActivateFunction {

    @Override
    public float process(float value) {
        if(value > 10f) return 1;
        if(value < -10f) return -1;
        if(value > -3e-4f && value < 3e-4f) return value;
//        float e2x = (float) Math.exp(2 * value);
//        return (e2x - 1)/(e2x + 1);
        return (float) Math.tanh(value);
    }

    @Override
    public float derivative(float value) {
        float result = process(value);
        return 1 - result*result;
//        return (1 + result) * (1 - result);
    }
}
