package ru.sashasuper.logic.neural;

import org.junit.jupiter.api.Test;
import ru.sashasuper.logic.*;
import ru.sashasuper.logic.functions.Identity;
import ru.sashasuper.logic.functions.Logistic;
import ru.sashasuper.logic.functions.ReLU;
import ru.sashasuper.logic.functions.TanH;

import java.util.Arrays;
import java.util.Random;

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


    @Test
    void linearSigmoid() {
        Network nn = new Network(3, 1, 0,
                new Matrix[]{ new Matrix(new float[][]{{5, -7, 0}}) }, new ReLU(), .01f);
        Vector half = new Vector(.5f, .5f, .5f);

        System.out.println("processResult = " + nn.process(half));
        System.out.println("nn = " + Arrays.toString(nn.getWeightMatrices()));
        for (int i = 0; i < 500; i++)
            nn.backPropagation(half, new Vector(1));
        System.out.println("nn = " + Arrays.toString(nn.getWeightMatrices()));
        System.out.println("processResult = " + nn.process(half));

        assert(Math.abs(nn.process(half).getValues()[0] - 1) < 0.1f);
    }

    void perform(Random r, Network nn) {
        float i = 1f / (r.nextInt(1000) + 1);
        assert(i >= 0 && i <= 1);
        Vector in = new Vector(i), out = new Vector(i / 2);
        nn.backPropagation(in, out);
    }

    @Test
    void linearLU() {
//        Network nn = new Network(1, 1, 1,
//                new Matrix[]{ new Matrix(new float[][]{{.5f}, {.2f}, {.3f}}), new Matrix(new float[][]{{.5f, 2f, .3f}}) },
//                new Logistic(), .1f);
        Network nn = new Network(1, 1, 0,
                new Matrix[]{ new Matrix(new float[][]{{-.1f}}) }, new Logistic(), 0.1f);
        Vector half = new Vector(.5f);
        Vector ones = new Vector(1);

        System.out.println("processResult = " + nn.process(half));
        Random r = new Random();
        for (int i = 0; i < 50000; i++) {
            if(i % 1000 == 0) System.out.println("nn = " + Arrays.toString(nn.getWeightMatrices()));
            perform(r, nn);
//            Neural.backPropagation(nn, half, new Vector(.5f));
//            Neural.backPropagation(nn, ones, new Vector(1));
        }
        System.out.println("nn = " + Arrays.toString(nn.getWeightMatrices()));
        System.out.println("processResult = " + nn.process(new Vector(0)));
        System.out.println("processResult = " + nn.process(new Vector(0.1f)));
        System.out.println("processResult = " + nn.process(new Vector(0.4f)));
        System.out.println("processResult = " + nn.process(new Vector(0.5f)));
        System.out.println("processResult = " + nn.process(new Vector(0.8f)));
        System.out.println("processResult = " + nn.process(new Vector(0.9f)));
        System.out.println("processResult = " + nn.process(new Vector(1f)));
    }
}