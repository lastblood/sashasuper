package ru.sashasuper.logic.functions;

import java.io.Serializable;

public class ReLU extends ActivateFunction implements Serializable {
    public ReLU(){}

    public float process(float value){
        return value > 0 ? value : 0;
    }

    public float derivative(float value){
        return value > 0 ? 1 : 0;
    }
}