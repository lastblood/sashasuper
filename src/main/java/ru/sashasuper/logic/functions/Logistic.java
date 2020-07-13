package ru.sashasuper.logic.functions;

import java.io.Serializable;

public class Logistic extends ActivateFunction implements Serializable {
    public Logistic(){}

    public float process(float value){
        return (float) (1 / (1 + Math.pow(Math.E, -value)));
        // В этом месте при большом значении value вылетал NaN
//        if(Float.isNaN(result) || Float.isInfinite(result)) {
//            System.out.printf("%f %f %f", value, ex, result);
//            System.exit(-1);
//        }
    }

    public float derivative(float value){
//        return process(value) * process(-value);

        // optimization with less accuracy
        float pr = process(value);
        return pr * (1 - pr);
    }
}
