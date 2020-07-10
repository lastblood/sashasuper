package ru.sashasuper.logic.neural;

import org.junit.jupiter.api.Test;
import ru.sashasuper.logic.*;
import ru.sashasuper.logic.functions.Identity;
import ru.sashasuper.logic.functions.Logistic;
import ru.sashasuper.logic.functions.ReLU;
import ru.sashasuper.logic.functions.TanH;
import ru.sashasuper.logic.generators.RandomMatrixGenerator;

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
//                System.out.println("nn = " + Arrays.toString(nn.getWeightMatrices()));
            for (AbstractMap.SimpleEntry<Vector, Vector> entry : entries) {
//                System.out.println("entry.getKey() = " + entry.getKey());
//                System.out.println("entry.getValue() = " + entry.getValue());
                nn.backPropagation(entry.getKey(), entry.getValue());
            }
        }

        System.out.println(Arrays.toString(nn.getWeightMatrices()));

        for(float i = 0; i <= 1.001; i += 0.05) {
            float j = i / 2;
            System.out.println(i + " " + nn.process(i).getValues()[0]);
//            assertTrue(Math.abs(nn.process(i).getValues()[0] - j) < 0.0001);
        }
    }

    @Test
    void halfMultiplierLinear() {
        Network nn = new Network(
                    new Matrix[]{new Matrix(new float[][]{{0.6f, 0.2f}})},
                new Identity(), 0.1f, true);
        halfMultiplier(nn, 150);
    }

    @Test
    void halfMultiplierLU() {
        Network nn = new Network(new Matrix[]{ new Matrix(new float[][]{{0.6f, 0.2f}}) },
                new ReLU(), 0.1f, true);
        halfMultiplier(nn, 150);
    }
}
