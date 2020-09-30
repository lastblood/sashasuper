package ru.sashasuper.logic.functions;

import ru.sashasuper.logic.Vector;

import java.io.Serializable;

@FunctionalInterface
public interface VectorFunction extends Serializable {
    Vector process(Vector operand);
}
