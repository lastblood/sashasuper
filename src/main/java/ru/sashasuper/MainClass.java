package ru.sashasuper;


import ru.sashasuper.io.Dataset;
import ru.sashasuper.io.IDXReader;
import ru.sashasuper.logic.Matrix;
import ru.sashasuper.logic.MomentumStat;
import ru.sashasuper.logic.Network;
import ru.sashasuper.logic.Vector;
import ru.sashasuper.logic.functions.Logistic;
import ru.sashasuper.logic.generators.RandomMatrixGenerator;

import java.io.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
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
                new Logistic(), 1f, true, Network.LOSS_FUNCTION.CROSS_ENTROPY);

//        Network nn = null;
//        try {
//            nn = (Network) new ObjectInputStream(new GZIPInputStream(
//                    new FileInputStream("backup100_98.nn"))).readObject();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//            System.exit(-10);
//        }

        nn.setRegularizationRate(0.1f);

        System.out.println("Generate");
        System.out.println(nn.mtTest(test));

//        ExecutorService service = Executors.newFixedThreadPool(2);

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

//            MomentumStat statTrain = nn.test(train);
//            MomentumStat statTest = entry.getKey();

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
//        service.shutdown();
    }

    public static Network trainNetwork(Network nn, Dataset train) {
        for (SimpleEntry<Vector, Vector> entry : train.getAll())
            nn.backPropagation(entry.getKey(), entry.getValue());

        return nn;
    }

    private static Network trainNetworkAverage(
            Network nn, Dataset train, Dataset test, int BATCHES, ExecutorService service)
                    throws CloneNotSupportedException, ExecutionException, InterruptedException {

        Future[] resultArray = new Future[BATCHES];
        Network[] nets = new Network[BATCHES];
        List<Dataset> batchesList = train.getBatches(BATCHES);

        for (int i = 0; i < BATCHES; i++) {
            Network currentNetwork = (Network) nn.clone();
            Dataset currentDataset = batchesList.get(i);
            nets[i] = currentNetwork;

            resultArray[i] = service.submit(() -> {
                Network network = trainNetwork(currentNetwork, currentDataset);
                MomentumStat stat = network.test(test);
                System.out.println(percent.apply(stat) + " " + stat);
                return network;
            });
        }

        for (int i = 0; i < BATCHES; i++)
            if(!resultArray[i].isDone())
                resultArray[i].get();

        int count = nn.getWeightMatrices().length;
        Matrix[] resultMatrices = new Matrix[count];
        Matrix[] tempMatrices = new Matrix[BATCHES];
        for (int i = 0; i < count; i++) {
            for (int j = 0; j < BATCHES; j++)
                tempMatrices[j] = nets[j].getWeightMatrices()[i];

            resultMatrices[i] = averageFrom(tempMatrices);
        }

        return new Network(resultMatrices, nn.getActivateFunction(), nn.getLearningRate(), nn.isWithBias());
    }

    private static Matrix averageFrom(Matrix ... matrices) {
        thr(Arrays.stream(matrices).anyMatch(m ->
                matrices[0].getColumns() != m.getColumns() || matrices[0].getRows() != m.getRows()));
        int rows = matrices[0].getRows();
        int columns = matrices[0].getColumns();
        float[][] result = new float[rows][columns];
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                float sum = 0;
                for (Matrix matrix : matrices)
                    sum += matrix.getValues()[y][x];
                result[y][x] = sum / matrices.length;
            }
        }
        return new Matrix(result);
    }
}
