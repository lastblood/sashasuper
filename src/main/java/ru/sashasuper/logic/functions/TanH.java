package ru.sashasuper.logic.functions;

import java.io.Serializable;

public class TanH extends ActivateFunction implements Serializable {
    public TanH(){
    }

    public float process(float value){
        float e2x = Math.getExponent(2*value);
        return (e2x - 1)/(e2x + 1);
    }

    public float derivative(float value){
        float e2x = Math.getExponent(2*value);
        return (2*(e2x - 1)*e2x)/((e2x + 1)*(e2x + 1)) + (2*e2x)/(e2x + 1);
    }
}
