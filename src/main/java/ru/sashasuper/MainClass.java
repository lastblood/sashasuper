package ru.sashasuper;


import ru.sashasuper.io.IDXReader;
import ru.sashasuper.logic.*;
import ru.sashasuper.logic.Vector;
import ru.sashasuper.logic.functions.*;
import ru.sashasuper.logic.generators.RandomMatrixGenerator;
import ru.sashasuper.utils.NanDefender;

import java.io.*;
import java.util.*;
import java.util.AbstractMap.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

import static ru.sashasuper.utils.Assertions.thr;

public class MainClass {
    public static SimpleEntry<MomentumStat, Network> generateGood(
            Dataset set, Supplier<Network> generator, Comparator<MomentumStat> metric, int iterations) {
        return Stream.generate(generator)
                .limit(iterations)
                .map(x -> new SimpleEntry<>(x.test(set), x))
                .min((x,y) -> metric.compare(x.getKey(), y.getKey()))
                .orElseThrow();
    }

    public static void main(String[] args) throws IOException, CloneNotSupportedException,
            InterruptedException, ExecutionException {
        Dataset train = new IDXReader("C:\\Java\\mnist\\train-images-idx3-ubyte.gz",
                "C:\\Java\\mnist\\train-labels-idx1-ubyte.gz",
                true, true).read();

        Dataset test = new IDXReader("C:\\Java\\mnist\\t10k-images-idx3-ubyte.gz",
                "C:\\Java\\mnist\\t10k-labels-idx1-ubyte.gz",
                true, true).read();

//        System.out.println("started");
//        long time = System.currentTimeMillis();
//        RandomMatrixGenerator gen = new RandomMatrixGenerator();
//        SimpleEntry<MomentumStat, Network> s = generateGood(train,
//                () -> new Network(gen.generateMatrices(true, -1, 1, 784, 10), new ReLU(), 1f, true),
//                Comparator.comparingDouble(x -> x.sumMetric), 10);
//
//        time = System.currentTimeMillis() - time;
//        System.out.println("Generation = " + time);
//
//        System.out.println(s.getKey());
//
//        Network nn = s.getValue();

        System.out.println("Read");

        Network nn = new Network(new RandomMatrixGenerator().generateMatrices(true, -0.5f, 0.5f, 784, 90, 10),
                new Logistic(), 1f, true);

        MomentumStat nnStat = nn.test(train);
        System.out.println(nnStat);

        float rate = 0.015f;
        float mult = 0.98f;

        List<SimpleEntry<Vector, Vector>> trainAll = train.getAll();

//        for (float value : trainAll.get(0).getKey().getValues()) {
//            if(value != 0.0f)
//                System.out.print(value + " ");
//        }

        while(rate > 0.004f) {
            nn.setLearningRate(rate);
            long time = System.currentTimeMillis();
            for (SimpleEntry<Vector, Vector> entry : trainAll)
                nn.backPropagation(entry.getKey(), entry.getValue());
            long time1 = System.currentTimeMillis() - time;
            rate *= mult;

            MomentumStat statBefore = nn.test(train);

            System.out.println(rate);
            System.out.println("BEFORE " + time1 + " " + statBefore);
            System.out.println("TEST " + nn.test(test));

//            if(statBefore.bad.size() * 4 < trainAll.size()) {
//                nn.setLearningRate(rate / 4);
//                for (SimpleEntry<Vector, Vector> entry : statBefore.bad)
//                    nn.backPropagation(entry.getKey(), entry.getValue());
//                System.out.println("AFTER " + (System.currentTimeMillis() - time) + " " + nn.test(train));
//                System.out.println("TEST " + nn.test(test));
//            }

//            if(rate < 0.003f)
//                mult = 1.1f;
        }

        try(ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(
                "C:\\Java\\sashasuper\\backup.nn")))) {
            oos.writeObject(nn);
            System.out.println("Wrote");
        }

        System.exit(2);

        ExecutorService service = Executors.newFixedThreadPool(4);

        int BATCHES = 4;
        int ITER_BY_EPOCH = 4;

        while(rate > 0.0001) {
            Future[] resultArray = new Future[BATCHES];
            Network[] nets = new Network[BATCHES];

            List<List<SimpleEntry<Vector, Vector>>> batches = train.getBatches(BATCHES);

            for (int i = 0; i < BATCHES; i++) {
                Network currentNetwork = (Network) nn.clone();
                currentNetwork.setLearningRate(rate);
                nets[i] = currentNetwork;

                List<SimpleEntry<Vector, Vector>> currentData = batches.get(i);
                resultArray[i] = service.submit(() -> trainNetworkRandom(
                        currentNetwork, currentData, train, currentData.size() / 8 / ITER_BY_EPOCH));
            }

            MomentumStat bestStat = null;
            Network bestNetwork = null;

            boolean[] done = new boolean[BATCHES];
            int countDone = 0;
            while(countDone < BATCHES) {
                for (int i = 0; i < done.length; i++) {
                    if(!done[i] && resultArray[i].isDone()) {
                        SimpleEntry current = (SimpleEntry) resultArray[i].get();
                        MomentumStat stat = (MomentumStat) current.getKey();
                        int metric = stat.countRight;

                        if(bestNetwork == null || bestStat.countRight < metric) {
                            bestStat = stat;
                            bestNetwork = (Network) current.getValue();
                        }

                        done[i] = true;
                        countDone++;

//                        System.out.println(i);
                    }
                }

                Thread.sleep(100);
            }

//            if(nnStat == null || bestStat.betterThan(nnStat)) {

            if(nnStat == null || bestStat.betterThan(nnStat)) {
                nn = bestNetwork;
                nnStat = bestStat;
                rate = Float.min(1, rate * 1.1f);
                System.out.println("GOOD " + bestStat);
            } else {
                rate *= 0.98;
                System.out.println("SAME " + rate);
            }

        }

        System.out.println(Arrays.toString(nn.getWeightMatrices()));
        service.shutdown();
    }

    static Random r = new Random();

    public static SimpleEntry<MomentumStat, Network> trainNetworkRandom(
            Network nn, List<SimpleEntry<Vector, Vector>> dataSetList, Dataset test, int count) {

        for (int j = 0; j < count; j++) {
            SimpleEntry<Vector, Vector> entry = dataSetList.get(r.nextInt(dataSetList.size()));
            nn.backPropagation(entry.getKey(), entry.getValue());
        }

        return new SimpleEntry<>(nn.test(test), nn);
    }

    public static SimpleEntry<MomentumStat, Network> trainNetwork(
            Network nn, List<SimpleEntry<Vector, Vector>> dataSetList, Dataset test, int iters) {
        for (int i = 0; i < iters; i++) {
//            System.out.println(i);
            for (SimpleEntry<Vector, Vector> entry : dataSetList) {
                nn.backPropagation(entry.getKey(), entry.getValue());
            }
        }

        return new SimpleEntry<>(nn.test(test), nn);
    }
}
