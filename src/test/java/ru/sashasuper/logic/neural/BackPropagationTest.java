package ru.sashasuper.logic.neural;

import org.junit.jupiter.api.Test;
import ru.sashasuper.logic.*;
import ru.sashasuper.logic.functions.*;
import ru.sashasuper.logic.generators.RandomMatrixGenerator;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class BackPropagationTest {
    @Test
    void linearIdentity() {
        Network nn = new Network(
                    new Matrix[]{ new Matrix(new float[][]{{5, -7, 0, 0.4f}}) },
                new Identity(), .05f, true);
        Vector half = new Vector(.5f, .5f, .5f);

        for (int i = 0; i < 200; i++) {
            nn.backPropagation(half, new Vector(1));
        }

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
            assertTrue(Math.abs(nn.process(i).getValues()[0] - j) < 0.001);
        }
    }

    @Test
    void halfMultiplierLinear() {
        Network nn = new Network(new Matrix[]{ new Matrix(new float[][]{{0.8f}}) }, new Identity(), 0.1f, false);
        halfMultiplier(nn, 50);
    }

    @Test
    void halfMultiplierReLU() {
        Network nn = new Network(new Matrix[]{ new Matrix(new float[][]{{0.8f}}) }, new ReLU(), 0.1f, false);
        halfMultiplier(nn, 50);
    }


    @Test
    void xor() {
        Random rand = new Random(101);
        RandomMatrixGenerator gen = new RandomMatrixGenerator(rand, 0.1f, 0.7f);

        Network nn = new Network(gen.generateMatrices(true, 2, 4, 2),
                new ReLU(), 0.03f, true);

        List<SimpleEntry<Vector, Vector>> vectors = List.of(
                new SimpleEntry<>(new Vector(0, 0), new Vector(1, 0)),
                new SimpleEntry<>(new Vector(0, 1), new Vector(0, 1)),
                new SimpleEntry<>(new Vector(1, 0), new Vector(0, 1)),
                new SimpleEntry<>(new Vector(1, 1), new Vector(1, 0))
        );

        for (int i = 0; i < 800; i++) {
            for (int j = 0; j < 4; j++) {
                SimpleEntry<Vector, Vector> entry = vectors.get(rand.nextInt(4));
                nn.backPropagation(entry.getKey(), entry.getValue());
            }
        }
        assertTrue(nn.test(vectors).sumMetric < 0.0001);
    }


    void hard(Network nn, int epochs) {
        System.out.println(nn.getActivateFunction());
        List<SimpleEntry<Vector, Vector>> vectors = List.of(
                new SimpleEntry<>(new Vector(0, 0, 0), new Vector(0)),
                new SimpleEntry<>(new Vector(0, 0, 1), new Vector(1)),
                new SimpleEntry<>(new Vector(0, 1, 0), new Vector(0)),
                new SimpleEntry<>(new Vector(0, 1, 1), new Vector(0)),
                new SimpleEntry<>(new Vector(1, 0, 0), new Vector(1)),
                new SimpleEntry<>(new Vector(1, 0, 1), new Vector(1)),
                new SimpleEntry<>(new Vector(1, 1, 0), new Vector(0)),
                new SimpleEntry<>(new Vector(1, 1, 1), new Vector(1))
        );

        float sum = 0;
        for(int i = 0; i <= epochs; i++) {
            for (SimpleEntry<Vector, Vector> entry : vectors)
                nn.backPropagation(entry.getKey(), entry.getValue());

            if(i % 10 == 0 && nn.test(vectors).sumMetric < 0.3f && vectors.stream().noneMatch(entry ->
                    (nn.process(entry.getKey()).getValues()[0] > 0.5) ^ (entry.getValue().getValues()[0] > 0.5))) {
                System.out.println(i);
                return;
            }
        }

        fail("Too slow learning " + nn.test(vectors).sumMetric);
    }

    @Test
    void rainTestLogistic() {
        Network nn = new Network(new RandomMatrixGenerator(new Random(0), -0.9f, 0.9f)
                    .generateMatrices(false, 3, 3, 1),
                new Logistic(), 0.2f, false);
        hard(nn, 1600);
    }

    @Test
    void rainTestReLU() {
        Network nn = new Network(new RandomMatrixGenerator(new Random(2),0.3f, 0.8f)
                    .generateMatrices(false, 3, 3, 1),
                new ReLU(), 0.03f, false);
        hard(nn, 800);
    }

    @Test
    void rainTestTanH() {
        Network nn = new Network(new RandomMatrixGenerator(new Random(0),0.3f, 0.8f)
                .generateMatrices(false, 3, 3, 1),
                new TanH(), 0.03f, false);
        hard(nn, 1000);
    }

    @Test
    void rainTestSoft() {
        Network nn = new Network(new RandomMatrixGenerator(new Random(0),0.3f, 0.8f)
                .generateMatrices(false, 3, 2, 1),
                new SoftPlus(), 0.03f, false);
        hard(nn, 3000);
    }
}