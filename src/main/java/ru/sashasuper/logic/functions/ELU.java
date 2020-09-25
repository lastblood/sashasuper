package ru.sashasuper.logic.functions;


import static java.lang.Math.pow;


public class ELU implements ElementActivateFunction{

    private float e = 2.718f, alpha=0.2f;

    public ELU() {}
        public ELU (float alpha) {
        this.alpha = alpha;
    }


    @Override
    public float derivative(float value) {
        if (value >= 0.0f) return 1;
        else return (float) (alpha*(pow(e, value)));
    }

    @Override
    public float process(float value) {
        if (value > 0.0f) return value;
        else return (float) (alpha*(pow(e, value)-1));
    }

}
