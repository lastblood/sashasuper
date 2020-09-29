package ru.sashasuper.demo;

import ru.sashasuper.io.Dataset;
import ru.sashasuper.io.IDXReader;
import ru.sashasuper.io.NetworkSerializer;
import ru.sashasuper.logic.Network;
import ru.sashasuper.logic.functions.Logistic;
import ru.sashasuper.logic.generators.XavierGenerator;
import ru.sashasuper.utils.MomentumStat;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.function.Function;
import java.util.zip.GZIPOutputStream;

public class LearnMNIST {
    static final Function<MomentumStat, Double> percent = stat ->
            (double) stat.countRight / (stat.countRight + stat.countWrong);

    public static final String trainImages = "train-images-idx3-ubyte.gz";
    public static final String trainLabels = "train-labels-idx1-ubyte.gz";
    public static final String testImages = "t10k-images-idx3-ubyte.gz";
    public static final String testLabels = "t10k-lables-idx1-ubyte.gz";

    public static final int middleCount = 500;
    public static final int toWrongCount = 300;

    public void learn(String directory) throws IOException {
        Dataset train = new IDXReader("C:\\Java\\mnist\\train-images-idx3-ubyte.gz",
                "C:\\Java\\mnist\\train-labels-idx1-ubyte.gz",
                true, true).read();

        Dataset test = new IDXReader("C:\\Java\\mnist\\t10k-images-idx3-ubyte.gz",
                "C:\\Java\\mnist\\t10k-labels-idx1-ubyte.gz",
                true, true).read();

        Network nn = Network.builder()
                .matrices(new XavierGenerator().generateMatrices(true, 784, middleCount, 10))
                .activation(new Logistic())
                .learningRate(0.5f)
                .lossFunction(Network.LOSS_FUNCTION.CROSS_ENTROPY)
                .regularizationRate(0.2f)
                .build();

        System.out.println("Generate");
        System.out.println(nn.mtTest(test));

        MomentumStat lastStat = null;

        float lr_multiplier = 1.03f;
//        float min_learning_rate = 0.003f;
        float max_learning_rate = 1.0f;

        while(nn.getLearningRate() < max_learning_rate && (lastStat == null || lastStat.countWrong > toWrongCount)) {
            long time = System.currentTimeMillis();
//            nn = trainNetwork(nn, train);
            train.getBatches(3000).forEach(nn::trainAtBatch);

            time = System.currentTimeMillis() - time;
            MomentumStat statTest = nn.mtTest(test);
            System.out.printf("%2.2f%% LR: %1.4f %dмс\n", percent.apply(statTest)*100, nn.getLearningRate(), time);
            System.out.printf("%s\n", statTest);

            nn.setLearningRate(nn.getLearningRate() * lr_multiplier);
            lastStat = statTest;
        }

        NetworkSerializer.serialization(String.format("backup%d_%d.nn", middleCount, toWrongCount), nn);
//        try(ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(
//                String.format("backup%d_%d.nn", middleCount, toWrongCount))))) {
//            oos.writeObject(nn);
//        }
        System.out.println("Wrote");
    }
}
