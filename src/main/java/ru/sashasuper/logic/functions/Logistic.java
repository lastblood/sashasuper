package ru.sashasuper.logic.functions;

import java.io.Serializable;

public class Logistic extends ActivateFunction implements Serializable {
    public Logistic(){}

    public float process(float value){
        float ex = Math.getExponent(value);
        return ex/(ex + 1);
    }

    public float derivative(float value){
        float pr = process(value);
        return pr/(1 - pr);
    }
}
