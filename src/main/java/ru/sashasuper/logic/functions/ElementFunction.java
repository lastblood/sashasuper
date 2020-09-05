package ru.sashasuper.logic.functions;

import ru.sashasuper.logic.Vector;

import java.util.Arrays;

@FunctionalInterface
public interface ElementFunction extends VectorFunction {
    @Override
    default Vector process(Vector operand) {
        float[] values = operand.getValues();
        float[] result = Arrays.copyOf(values, values.length);

        for (int i = 0; i < operand.getNonBiasedLength(); i++)
            result[i] = process(result[i]);

        return new Vector(true, result);
    }

    float process(float value);
}
