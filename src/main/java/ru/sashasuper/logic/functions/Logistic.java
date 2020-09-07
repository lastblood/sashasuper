package ru.sashasuper.logic.functions;

public class Logistic implements ElementActivateFunction {

    @Override
    public float process(float value){
        return (float) (1 / (1 + Math.exp(-value)));
    }

    @Override
    public float derivative(float value){
//        return process(value) * process(-value);

        // optimization with less accuracy
        float pr = process(value);
        return pr * (1 - pr);
    }
}
