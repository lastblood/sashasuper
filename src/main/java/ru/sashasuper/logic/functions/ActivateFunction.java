package ru.sashasuper.logic.functions;


import java.io.Serializable;

public abstract class ActivateFunction implements Serializable {
    public abstract float process(float value);
    public abstract float derivative(float value);
}
