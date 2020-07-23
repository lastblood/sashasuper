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

        Network nn = new Network(new RandomMatrixGenerator(new Random(143))
                .generateMatrices(true, -0.01f, 0.01f, 784, 100, 10), new Logistic(), 1f);

        System.out.println("Generate");
//        MomentumStat nnStat = nn.test(train); ss
//        MomentumStat nnStat = nn.test(train);
//        System.out.println(nn.test(train));
        System.out.println(nn.test(test));

        ExecutorService service = Executors.newFixedThreadPool(2);

        float learning_rate = 0.2f;
        float lr_multiplier = 0.8f;
        float min_learning_rate = 0.0001f;

        MomentumStat lastStat = null;

        int fails = 0;

        while(learning_rate > min_learning_rate && fails < 5) {
            nn.setLearningRate(learning_rate);
            long time = System.currentTimeMillis();
            try {
//                SimpleEntry<MomentumStat, Network> entry =
//                        trainNetworkBestLR(nn, train, test, 2, service);
            nn = trainNetwork(nn, train);
//            nn = trainNetworkAverage(nn, train, test, 2, service);

//                nn = entry.getValue();
//                System.out.printf("(%f) REAL: %f; time = %d\n",
//                        learning_rate, nn.getLearningRate(), (System.currentTimeMillis() - time));

//                MomentumStat statBefore = nn.test(train);
//                System.out.println(percent.apply(statBefore) + " " + statBefore);

                time = System.currentTimeMillis() - time;
//                MomentumStat statTest = entry.getKey();
                MomentumStat statTest = nn.test(test);
                System.out.printf("%f %dмс\n", learning_rate, time);
                System.out.println(percent.apply(statTest)*100 + "% " + statTest);


                if(lastStat != null && learning_rate > 0.005) {
                    int diff = statTest.countRight - lastStat.countRight;
                    if(diff < -20)
                        learning_rate *= 0.1;
                    else if(diff < 10)
                        learning_rate *= 0.5;
                    else
                        learning_rate *= 0.9;
                } else
                    learning_rate *= 0.9;

                lastStat = statTest;
//                System.out.println(Arrays.toString(nn.getWeightMatrices()));
//                learning_rate *= lr_multiplier;
            } catch (Exception e) {
                e.printStackTrace();
                fails++;
            }
        }

        if(fails < 4)
        try(ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(
                "backup.nn")))) {
            oos.writeObject(nn);
            System.out.println("Wrote");
        }
        service.shutdown();
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

    private static SimpleEntry<MomentumStat, Network> trainNetworkBest(
            Network nn, Dataset train, int BATCHES, float rate, ExecutorService service)
                    throws CloneNotSupportedException, ExecutionException, InterruptedException {

        Future[] resultArray = new Future[BATCHES];
        Network[] nets = new Network[BATCHES];

        for (int i = 0; i < BATCHES; i++) {
            Network currentNetwork = (Network) nn.clone();
            currentNetwork.setLearningRate(rate);
            nets[i] = currentNetwork;

            resultArray[i] = service.submit(() -> trainNetwork(currentNetwork, train));
        }

        MomentumStat bestStat = null;
        Network bestNetwork = null;

        int countDone = 0;
        while(countDone < BATCHES) {
            for (int i = 0; i < BATCHES; i++) {
                if(resultArray[i].isDone()) {
                    SimpleEntry current = (SimpleEntry) resultArray[i].get();
                    MomentumStat stat = (MomentumStat) current.getKey();
                    int metric = stat.countRight;

                    if(bestNetwork == null || bestStat.countRight < metric) {
                        bestStat = stat;
                        bestNetwork = (Network) current.getValue();
                    }

                    countDone++;
                }
            }

            Thread.sleep(50);
        }

        return new SimpleEntry<>(bestStat, bestNetwork);
    }
}
