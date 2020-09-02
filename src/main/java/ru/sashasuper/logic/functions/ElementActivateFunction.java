package ru.sashasuper.logic.functions;

import ru.sashasuper.logic.Vector;

import java.util.Arrays;

public interface ElementActivateFunction extends ActivateFunction, ElementFunction {
    @Override
    default Vector derivative(Vector operand) {
        float[] values = operand.getValues();
        float[] result = Arrays.copyOf(values, values.length);

        for (int i = 0; i < operand.getNonBiasedLength(); i++)
            result[i] = derivative(result[i]);

        return new Vector(true, result);
    }

    float derivative(float value);
}
