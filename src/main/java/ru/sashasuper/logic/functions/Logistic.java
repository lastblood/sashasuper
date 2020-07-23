package ru.sashasuper.logic.functions;

import java.io.Serializable;

public class Logistic implements Serializable, ActivateFunction {
    public Logistic(){}

    public float process(float value){
        return (float) (1 / (1 + Math.exp(-value)));
    }

    public float derivative(float value){
//        return process(value) * process(-value);

        // optimization with less accuracy
        float pr = process(value);
        return pr * (1 - pr);
    }
}
