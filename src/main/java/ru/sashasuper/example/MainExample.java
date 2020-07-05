package ru.sashasuper.example;

import ru.sashasuper.logic.*;
import ru.sashasuper.logic.functions.*;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainExample {
    public static void main(String[] args) {
        ArrayList<AbstractMap.SimpleEntry<Vector, Vector>> list = new ArrayList<>(4);
        addTo(list, new float[]{0,0}, new float[]{0});
        addTo(list, new float[]{0,1}, new float[]{0});
        addTo(list, new float[]{1,0}, new float[]{0});
        addTo(list, new float[]{1,1}, new float[]{1});
        Dataset data = new SimpleDataset(list);

        int layerCount = 1;
        Matrix[] weightMatrices = new Matrix[layerCount + 1];
        MatrixGenerator matrixGenerator = new MatrixGenerator();
        weightMatrices[0] = matrixGenerator.randomMatrix(2, 2, -2, 2);
        weightMatrices[1] = matrixGenerator.randomMatrix(1, 2, -2, 2);

        System.out.println("weightMatrices0 = " + Arrays.deepToString(weightMatrices[0].getValues()));
        System.out.println("weightMatrices1 = " + Arrays.deepToString(weightMatrices[1].getValues()));
        Network net = new Network(weightMatrices, new Logistic(), 0.05f);

        System.out.println("test0 " + net.test(data));

        for (int i = 0; i < 100; i++) {
            net.learnIteration(data);
        }

        System.out.println("weightMatrices0 = " + Arrays.deepToString(net.getWeightMatrices()[0].getValues()));
        System.out.println("weightMatrices1 = " + Arrays.deepToString(net.getWeightMatrices()[1].getValues()));

        System.out.println("test1 " + net.test(data));
    }

    private static void addTo(List<AbstractMap.SimpleEntry<Vector, Vector>> list, float[] first, float[] second) {
        list.add(new AbstractMap.SimpleEntry<>(new Vector(first), new Vector(second)));
    }
}
