package ru.sashasuper.logic.functions;


import java.io.Serializable;

public abstract class ActivateFunction extends ElementFunction implements Serializable {
    public abstract float derivative(float value);
}
