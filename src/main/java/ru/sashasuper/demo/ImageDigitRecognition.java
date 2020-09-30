package ru.sashasuper.demo;

import ru.sashasuper.io.*;
import ru.sashasuper.logic.*;
import ru.sashasuper.logic.functions.ElementFunction;

import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;

import static ru.sashasuper.utils.Assertions.thr;

public class ImageDigitRecognition {
    public void recognize(String propertiesPath, String neuralNetworkPath) throws IOException {
        Network nn;
        try {
            nn = NetworkSerializer.deserialization(neuralNetworkPath);
        } catch (ClassNotFoundException e) {
            System.out.println("Different class version. You can just change UID");
            throw new IllegalArgumentException(e);
        }

        ImageReader imageReader = new ImageReader(propertiesPath, true);
        imageReader.setScale(28, 28);
        imageReader.setKeyMapper(((ElementFunction) x -> x / 256)::process);
        imageReader.setValueMapper(v -> v.getNonBiasedLength() == 1 ?
                IDXDataset.vectorWithOneAtPosition(Math.round(v.getValues()[0]), 10) : v);

        Dataset data = imageReader.read();
        Map<String, Vector> map = imageReader.getFileToVector();

        data.getAll().forEach(entry -> {
            Map.Entry<String, Vector> stringVectorEntry = map.entrySet().stream()
                    .filter(x -> x.getValue().equals(entry.getKey()))
                    .findFirst()
                    .orElseThrow();

            String imageFileName = stringVectorEntry.getKey();

            Vector result = nn.process(entry.getKey());
            Vector should = entry.getValue();

            float mse = VectorMath.MSE(result, should);
            boolean isRight = Network.isRightClassificated(new SimpleEntry<>(result, should));

            System.out.printf("%d '%s'; mse=%f; %b\n",
                    Network.getMaxIndex(should),
                    imageFileName, mse, isRight);

            for (int i = 0; i < result.getNonBiasedLength(); i++) {
                System.out.printf("%d=%2.2f%% ", i, result.getValues()[i] * 100);
            }
            System.out.println("\n");
        });
    }

    public static void main(String[] args) throws IOException {
        thr(args.length < 2,
            "Args[0] should contains path to properties file, " +
                    "Args[1] should contains path to serialized neural network");
        new ImageDigitRecognition().recognize(args[0], args[1]);
    }
}
