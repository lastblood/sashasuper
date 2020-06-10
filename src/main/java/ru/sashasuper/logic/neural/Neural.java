package ru.sashasuper.logic.neural;

import ru.sashasuper.logic.Network;
import ru.sashasuper.logic.Vector;

import static ru.sashasuper.logic.VectorMath.multMatrixVector;
import static ru.sashasuper.utils.Assertions.thr;

public class Neural {
    public static Vector process(Network network, Vector input) {
        Vector[] vectors = processUniversally(network, input, false);
        thr(vectors.length != 1 || vectors[0] == null);
        return vectors[0];
    }

    private static Vector[] processUniversally(Network network, Vector input, boolean rememberSteps) {
        thr(network.getWeightMatrices()[0].getColumns() != input.getLen(),
                "input.length != matrix[0].columns");

        Vector[] memories = new Vector[network.getLayerCount() + 1];
        memories[0] = input;

        Vector currentVector = input;
        for (int i = 0; i < network.getWeightMatrices().length; i++) {
            currentVector = multMatrixVector(network.getWeightMatrices()[i], currentVector);
            //todo: применить функцию активации, иначе магии нейронных сетей нам не видать
            if (rememberSteps) memories[i + 1] = currentVector;
        }

        return rememberSteps ? memories : new Vector[]{currentVector};
    }

    public static Neural backPropagation(Network network, Vector input, Vector expectedOutput) {
        Vector[] vectors = processUniversally(network, input, true);
        thr(vectors.length != network.getLayerCount() + 1);
        return null; //todo
    }
}
