package ru.sashasuper.logic.functions;

import java.io.Serializable;

public class Sigmoid extends ActivateFunction implements Serializable {
    public Sigmoid(){}

    public float process(float value){
        float ex = Math.getExponent(value);
        return ex/(ex + 1);
    }

    public float derivative(float value){
        float ex = Math.getExponent(value);
        float e2x = Math.getExponent(2*value);
        return ex/(ex + 1) - e2x/(e2x + 2*ex + 1);
    }
}
