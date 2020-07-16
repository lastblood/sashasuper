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

        Network nn = new Network(new RandomMatrixGenerator().generateMatrices(true, -0.5f, 0.5f, 784, 300, 10),
                new Logistic(), 1f, true);

        MomentumStat nnStat = nn.test(train);
        System.out.println(nnStat);

        ExecutorService service = Executors.newFixedThreadPool(4);
        int BATCHES = 4;
        int ITER_BY_EPOCH = 4;

        float learning_rate = 0.005f;
        float lr_multiplier = 0.97f;
        float min_learning_rate = 0.001f;

        List<SimpleEntry<Vector, Vector>> trainAll = train.getAll();

        while(learning_rate > min_learning_rate) {
            nn.setLearningRate(learning_rate);
            long time = System.currentTimeMillis();
            for (SimpleEntry<Vector, Vector> entry : trainAll)
                nn.backPropagation(entry.getKey(), entry.getValue());
            learning_rate *= lr_multiplier;

            MomentumStat statBefore = nn.test(train);

            System.out.println(learning_rate + " " + (System.currentTimeMillis() - time));
            System.out.print((double) percent.apply(statBefore) + " " + statBefore);
            MomentumStat statTest = nn.test(test);
            System.out.print("TEST " + (double) percent.apply(statTest) + " " + statTest);

            // Отдельно пробег по худшим
        }

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
            Network nn, Dataset train, int BATCHES, float rate, ExecutorService service)
                    throws CloneNotSupportedException, ExecutionException, InterruptedException {

        Future[] resultArray = new Future[BATCHES];
        Network[] nets = new Network[BATCHES];
        List<Dataset> batchesList = train.getBatches(BATCHES);

        for (int i = 0; i < BATCHES; i++) {
            Network currentNetwork = (Network) nn.clone();
            Dataset currentDataset = batchesList.get(i);

            currentNetwork.setLearningRate(rate);
            nets[i] = currentNetwork;

            resultArray[i] = service.submit(() -> trainNetwork(currentNetwork, currentDataset));
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
        thr(Arrays.stream(matrices).allMatch(m ->
                matrices[0].getColumns() == m.getColumns() && matrices[0].getRows() == m.getRows()));
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
