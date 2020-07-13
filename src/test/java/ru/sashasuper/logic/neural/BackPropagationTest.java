package ru.sashasuper.logic.neural;

import org.junit.jupiter.api.Test;
import ru.sashasuper.logic.*;
import ru.sashasuper.logic.functions.*;
import ru.sashasuper.logic.generators.RandomMatrixGenerator;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.List;
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
        Network nn = new Network(new Matrix[]{ new Matrix(new float[][]{{0.8f}}) }, new Identity(), 0.1f);
        halfMultiplier(nn, 50);
    }

    @Test
    void halfMultiplierReLU() {
        Network nn = new Network(new Matrix[]{ new Matrix(new float[][]{{0.8f}}) }, new ReLU(), 0.1f);
        halfMultiplier(nn, 50);
    }



    // TODO: 07.07.2020 fix
//    @Test
    void xor() {
        RandomMatrixGenerator gen = new RandomMatrixGenerator(new java.util.Random(100), 0.01f, 1f);

        Network nn = new Network(new RandomMatrixGenerator(0.1f, 0.8f).generateMatrices(true, 2, 4, 4, 1),
                new ReLU(), 0.1f, true);

        SimpleEntry<Vector, Vector>[] vectors = new SimpleEntry[]{
                new SimpleEntry<>(new Vector(0, 0), new Vector(0)),
                new SimpleEntry<>(new Vector(0, 1), new Vector(1)),
                new SimpleEntry<>(new Vector(1, 0), new Vector(1)),
                new SimpleEntry<>(new Vector(1, 1), new Vector(0))
        };

        for (int i = 0; i < 100; i++) {
            for (SimpleEntry<Vector, Vector> entry : vectors) {
                nn.backPropagation(entry.getKey(), entry.getValue());
            }
        }

//        System.out.println("nn = " + Arrays.toString(nn.getWeightMatrices()));

        for (SimpleEntry<Vector, Vector> vector : vectors) {
            System.out.print(vector.getKey() + " : ");
            System.out.println(nn.process(vector.getKey()).getValues()[0]);
        }
    }


    void hard(Network nn, int epochs) {
        SimpleEntry<Vector, Vector>[] vectors = new SimpleEntry[]{
                new SimpleEntry<>(new Vector(0, 0, 0), new Vector(0)),
                new SimpleEntry<>(new Vector(0, 0, 1), new Vector(1)),
                new SimpleEntry<>(new Vector(0, 1, 0), new Vector(0)),
                new SimpleEntry<>(new Vector(0, 1, 1), new Vector(0)),
                new SimpleEntry<>(new Vector(1, 0, 0), new Vector(1)),
                new SimpleEntry<>(new Vector(1, 0, 1), new Vector(1)),
                new SimpleEntry<>(new Vector(1, 1, 0), new Vector(0)),
                new SimpleEntry<>(new Vector(1, 1, 1), new Vector(1))
        };

        float sum = 0;
        for(int i = 0; i <= epochs; i++) {
            for (SimpleEntry<Vector, Vector> entry : vectors) {
                nn.backPropagation(entry.getKey(), entry.getValue());
            }

            if(i % 100 == 0) {
                sum = 0;
                for (SimpleEntry<Vector, Vector> entry : vectors) {
                    Vector r = nn.process(entry.getKey());
                    sum += VectorMath.MSE(r, entry.getValue());
                }

                System.out.println(sum);
                if(sum < 0.01f) return;
            }
        }

        System.out.println(Arrays.toString(nn.getWeightMatrices()));
        System.out.println("MSE = " + sum);
        for (SimpleEntry<Vector, Vector> entry : vectors) {
            System.out.print(Arrays.toString(entry.getKey().getNonBiasedValues()));
            System.out.print("(" + entry.getValue().getValues()[0] + ") : ");
            System.out.println(nn.process(entry.getKey()).getValues()[0]);
        }
        fail("Too slow learning");
    }

    @Test
    void rainTestLogistic() {
        Network nn = new Network(new RandomMatrixGenerator(-0.9f, 0.9f).generateMatrices(false, 3, 3, 1),
                new Logistic(), 0.1f, false);
        hard(nn, 5000);
    }

    // TODO: 11.07.2020 fix
//    @Test
//    void rainTestReLU() {
//        Network nn = new Network(new RandomMatrixGenerator(0.5f, 0.51f).generateMatrices(false, 3, 10, 5, 3, 1),
//                new ReLU(), 0.8f, false);
//        hard(nn, 2000);
//    }
}