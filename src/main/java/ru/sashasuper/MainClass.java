package ru.sashasuper;


import ru.sashasuper.io.Dataset;
import ru.sashasuper.io.IDXReader;
import ru.sashasuper.logic.*;
import ru.sashasuper.logic.Vector;
import ru.sashasuper.logic.functions.*;
import ru.sashasuper.logic.generators.RandomMatrixGenerator;

import java.io.*;
import java.util.*;
import java.util.AbstractMap.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.*;
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
                    .generateMatrices(true, -0.01f, 0.01f, 784, 300, 10),
                new Logistic(), 1f);

        System.out.println("Generate");
        System.out.println(nn.test(test));

//        ExecutorService service = Executors.newFixedThreadPool(2);

        float learning_rate = 1f;
        float lr_multiplier = 0.9f;
        float min_learning_rate = 0.003f;

        MomentumStat lastStat = null;

        while(learning_rate > min_learning_rate) {
            nn.setLearningRate(learning_rate);
            long time = System.currentTimeMillis();
//            nn = trainNetwork(nn, train);
            train.getBatches(6000).forEach(nn::trainAtBatch);

//            nn = trainNetworkAverage(nn, train, test, 2, service);

//            MomentumStat statTrain = nn.test(train);
//            MomentumStat statTest = entry.getKey();

            time = System.currentTimeMillis() - time;
            MomentumStat statTest = nn.test(test);
            System.out.printf("%2.2f%% LR: %1.4f %dмс\n", percent.apply(statTest)*100, learning_rate, time);
            System.out.printf("%s\n", statTest);


//            if(lastStat != null && learning_rate > 0.005) {
//                int diff = statTest.countRight - lastStat.countRight;
//                if(diff < -20)
//                    learning_rate *= 0.1;
//                else if(diff < -5)
//                    learning_rate *= 0.5;
//                else
//                    learning_rate *= lr_multiplier;
//            } else
                learning_rate *= lr_multiplier;

            lastStat = statTest;
        }

        try(ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(
                "backup.nn")))) {
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
