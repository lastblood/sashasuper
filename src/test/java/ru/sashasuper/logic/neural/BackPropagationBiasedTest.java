package ru.sashasuper.logic.neural;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.sashasuper.logic.Matrix;
import ru.sashasuper.logic.Network;
import ru.sashasuper.logic.Vector;
import ru.sashasuper.logic.functions.Identity;
import ru.sashasuper.logic.functions.ReLU;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class BackPropagationBiasedTest {

    void halfMultiplier(Network nn, int epochs) {
        List<AbstractMap.SimpleEntry<Vector, Vector>> entries = IntStream.range(0, 11).mapToObj(
                x -> new AbstractMap.SimpleEntry<>(new Vector((float) x / 10), new Vector((float) x / 20)))
                .collect(Collectors.toList());

        for (int i = 0; i <= epochs; i++) {
            if(i % 10 == 0)
            for (AbstractMap.SimpleEntry<Vector, Vector> entry : entries) {
                nn.backPropagation(entry.getKey(), entry.getValue());
            }
        }

        System.out.println(Arrays.toString(nn.getWeightMatrices()));

        for(float i = 0; i <= 1.001; i += 0.05) {
            float j = i / 2;
//            System.out.println(i + " " + nn.process(i).getValues()[0]);
            assertTrue(Math.abs(nn.process(i).getValues()[0] - j) < 0.0001);
        }
    }

    @Test
    void halfMultiplierLinear() {
        Network nn = new Network(
                    new Matrix[]{new Matrix(new float[][]{{0.6f, 0.0f}})},
                new Identity(), 0.07f, true);
        halfMultiplier(nn, 5000);
    }

    @Test
    void halfMultiplierLU() {
        Network nn = new Network(new Matrix[]{ new Matrix(new float[][]{{0.6f, 0.2f}}) },
                new ReLU(), 0.1f, true);
        halfMultiplier(nn, 5000);
    }
}
