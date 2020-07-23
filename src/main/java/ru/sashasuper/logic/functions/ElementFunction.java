package ru.sashasuper.logic.functions;


import java.io.Serializable;

@FunctionalInterface
public interface ElementFunction extends Serializable {
    float process(float value);
}
