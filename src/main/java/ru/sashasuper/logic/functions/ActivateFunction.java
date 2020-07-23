package ru.sashasuper.logic.functions;


import java.io.Serializable;

public interface ActivateFunction extends ElementFunction, Serializable {
    float derivative(float value);
}
