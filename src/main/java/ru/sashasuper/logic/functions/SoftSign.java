package ru.sashasuper.logic.functions;


import static java.lang.Math.abs;
import static java.lang.Math.pow;

public class SoftSign implements ElementActivateFunction{

    private float negativeMultiplier=-2.2f, positiveMultiplier=5.0f;

    public SoftSign() {}
    public SoftSign (float negativeMultiplier, float positiveMultiplier) {
        this.negativeMultiplier = negativeMultiplier;
        this.positiveMultiplier = positiveMultiplier;
    }

    @Override
    public float derivative(float value) {

        return (float) (1/(1+pow(abs(value), 2)));
    }

    @Override
    public float process(float value) {
        return (float) (value/(1+abs(value)));
    }

}
