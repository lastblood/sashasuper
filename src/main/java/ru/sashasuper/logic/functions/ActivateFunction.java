package ru.sashasuper.logic.functions;


import ru.sashasuper.logic.Vector;

public interface ActivateFunction extends VectorFunction {
    Vector derivative(Vector operand);
}
