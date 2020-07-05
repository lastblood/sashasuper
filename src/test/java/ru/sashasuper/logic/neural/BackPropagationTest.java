package ru.sashasuper.logic.neural;

import org.junit.jupiter.api.Test;
import ru.sashasuper.logic.*;
import ru.sashasuper.logic.functions.*;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class BackPropagationTest {
    @Test
    void linearIdentity() {
        Network nn = new Network(3, 1, 0,
            new Matrix[]{ new Matrix(new float[][]{{5, -7, 0}}) }, new Identity(), .05f);
        Vector half = new Vector(.5f, .5f, .5f);

//        System.out.println("processResult = " + Neural.process(nn, half));
//        System.out.println("nn = " + Arrays.toString(nn.getWeightMatrices()));
        for (int i = 0; i < 200; i++)
            nn.backPropagation(half, new Vector(1));
//        System.out.println("nn = " + Arrays.toString(nn.getWeightMatrices()));
//        System.out.println("processResult = " + Neural.process(nn, half));

        assert(Math.abs(nn.process(half).getValues()[0] - 1) < 0.01f);
    }


    void halfMultiplier(Network nn, int epochs) {
        List<SimpleEntry<Vector, Vector>> entries = IntStream.range(0, 11).mapToObj(
                x -> new SimpleEntry<>(new Vector((float) x / 10), new Vector((float) x / 20)))
                .collect(Collectors.toList());

        for (int i = 0; i <= epochs; i++) {
            if(i % 10 == 0)
                System.out.println("nn = " + Arrays.toString(nn.getWeightMatrices()));
            for (SimpleEntry<Vector, Vector> object : entries)
                nn.backPropagation(object.getKey(), object.getValue());
        }

        for(float i = 0; i <= 1; i += 0.001) {
            float j = i / 2;
            assertTrue(Math.abs(nn.process(i).getValues()[0] - j) < 0.0001);
        }
    }

    @Test
    void halfMultiplierLinear() {
        Network nn = new Network(1, 1, 0,
                new Matrix[]{ new Matrix(new float[][]{{1}}) }, new Identity(), 0.1f);

        halfMultiplier(nn, 30);
    }

    @Test
    void halfMultiplierLU() {
        Network nn = new Network(1, 1, 0,
                new Matrix[]{ new Matrix(new float[][]{{1}}) }, new ReLU(), 0.1f);

        halfMultiplier(nn, 30);
    }


    @Test
    void xor() {
        MatrixGenerator gen = new MatrixGenerator(new java.util.Random(100));

        Network nn = new Network(2, 1, 2, new Matrix[]{
                gen.randomMatrix(4, 2, 0.1f, 1),
                gen.randomMatrix(4, 4, 0.1f, 1),
                gen.randomMatrix(1, 4, 0.1f, 1)},
                new ReLU(), 0.01f);

        SimpleEntry<Vector, Vector>[] vectors = new SimpleEntry[]{
                new SimpleEntry<>(new Vector(0, 0), new Vector(0)),
                new SimpleEntry<>(new Vector(0, 1), new Vector(1)),
                new SimpleEntry<>(new Vector(1, 0), new Vector(1)),
                new SimpleEntry<>(new Vector(1, 1), new Vector(0))
        };

        for (int i = 0; i < 10000; i++) {
            for (SimpleEntry<Vector, Vector> entry : vectors) {
                nn.backPropagation(entry.getKey(), entry.getValue());
            }
        }

        for (SimpleEntry<Vector, Vector> vector : vectors) {
            System.out.println(nn.process(vector.getKey()));
        }
    }
}