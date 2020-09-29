package ru.sashasuper;


import ru.sashasuper.io.Dataset;
import ru.sashasuper.io.IDXReader;
import ru.sashasuper.utils.MomentumStat;
import ru.sashasuper.logic.Network;
import ru.sashasuper.logic.functions.Logistic;
import ru.sashasuper.logic.generators.RandomMatrixGenerator;

import java.io.*;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.zip.GZIPOutputStream;

import static ru.sashasuper.utils.Assertions.thr;

public class MainClass {
    static final Function<MomentumStat, Double> percent = stat ->
            (double) stat.countRight / (stat.countRight + stat.countWrong);

    public static void main(String[] args) throws IOException, CloneNotSupportedException,
            InterruptedException, ExecutionException {

        Dataset train = new IDXReader("C:\\Java\\mnist\\train-images-idx3-ubyte.gz",
                "C:\\Java\\mnist\\train-labels-idx1-ubyte.gz",
                true, true).read();

        Dataset test = new IDXReader("C:\\Java\\mnist\\t10k-images-idx3-ubyte.gz",
                "C:\\Java\\mnist\\t10k-labels-idx1-ubyte.gz",
                true, true).read();

        Network nn = new Network(new RandomMatrixGenerator(new Random(15123))
                    .generateMatrices(true, -0.1f, 0.1f, 784, 100, 10),
                new Logistic(), 1f, 0.1f, true, Network.LOSS_FUNCTION.CROSS_ENTROPY, 60000);

//        Network nn = null;
//        try {
//            nn = (Network) new ObjectInputStream(new GZIPInputStream(
//                    new FileInputStream("backup100_98.nn"))).readObject();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//            System.exit(-10);
//        }

        System.out.println("Generate");
        System.out.println(nn.mtTest(test));

        float learning_rate = 0.3f;
        float lr_multiplier = 1.03f;
        float min_learning_rate = 0.003f;
        float max_learning_rate = 1.0f;

        MomentumStat lastStat = null;

//        while(learning_rate > min_learning_rate && (lastStat == null || lastStat.countWrong > 200)) {
        while(learning_rate < max_learning_rate && (lastStat == null || lastStat.countWrong > 500)) {
            nn.setLearningRate(learning_rate);

            long time = System.currentTimeMillis();
//            nn = trainNetwork(nn, train);
            train.getBatches(3000).forEach(nn::trainAtBatch);

            time = System.currentTimeMillis() - time;
            MomentumStat statTest = nn.mtTest(test);
            System.out.printf("%2.2f%% LR: %1.4f %dмс\n", percent.apply(statTest)*100, learning_rate, time);
            System.out.printf("%s\n", statTest);

            learning_rate *= lr_multiplier;
            lastStat = statTest;
        }

        try(ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(
                "backup100_985.nn")))) {
            oos.writeObject(nn);
            System.out.println("Wrote");
        }
    }
}
