package ru.sashasuper.logic.functions;


import static java.lang.Math.abs;
import static java.lang.Math.pow;

public class SoftSign implements ElementActivateFunction{

    @Override
    public float derivative(float value) {

        return (float) (1/pow((1+abs(value)), 2));
    }

    @Override
    public float process(float value) {
        return value/(1+abs(value));
    }

}
