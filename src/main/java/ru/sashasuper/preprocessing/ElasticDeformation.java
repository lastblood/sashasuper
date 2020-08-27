package ru.sashasuper.preprocessing;

import ru.sashasuper.logic.Vector;

import java.awt.*;
import java.util.*;

import static ru.sashasuper.utils.Assertions.thr;

public class ElasticDeformation {
    // Радиус ядра свёртки в пикселях (без учёта центра)
    // У свёртки 5x5 радиус 2
    private int sigmaRaduis;

    // Множитель случайного сдвига пикселей.
    // Используется после применения гауссовой свёртки к матрице весов
    private float alphaMultiplier;

    private final Random random;

    public ElasticDeformation(int sigmaRaduis, float alphaMultiplier, Random random) {
        this.sigmaRaduis = sigmaRaduis;
        this.alphaMultiplier = alphaMultiplier;
        this.random = random;
    }

    public ElasticDeformation(int sigmaRaduis, float alphaMultiplier) {
        this.sigmaRaduis = sigmaRaduis;
        this.alphaMultiplier = alphaMultiplier;
        this.random = new Random();
    }


    public Vector deformate(Vector input, Dimension size) {
        return elasticDeformation(input, size, size, sigmaRaduis, alphaMultiplier, random);
    }

    private static Map<Integer, float[]> kernelHash = new HashMap<>();
    public static synchronized float[] getGaussianKernel(int radius) {
        if(!kernelHash.containsKey(radius))
            kernelHash.put(radius, computeGaussianKernel(radius));

        return kernelHash.get(radius);
    }

    private static float[] computeGaussianKernel(int radius) {
        System.out.println("computed for raduis " + radius);
        int a = 2 * radius + 1;
        float disp = (a / 2f) * (a / 2f);
        float[] kernel = new float[a * a];

        for (int x = -radius; x <= radius; x++)
            for (int y = -radius; y <= radius; y++)
                kernel[(y + radius) * a + x + radius] =
                        (float) ((1 / (2 * Math.PI * disp)) *
                                Math.exp(((-(x * x + y * y)) / (2 * disp))));

        kernelHash.put(radius, kernel);
        return kernel;
    }

    private static float[] gaussianFilter(float[] matrix, Dimension vectorDim, int kernelRadius) {
        thr(matrix.length != vectorDim.height * vectorDim.width);

        float[] result = new float[matrix.length];
        float[] kernel = getGaussianKernel(kernelRadius);

        for (int mainX = 0; mainX < vectorDim.width; mainX++) {
            for (int mainY = 0; mainY < vectorDim.height; mainY++) {
                float sum = 0;

                int a = kernelRadius*2 + 1;
                for (int kernelX = 0; kernelX < a; kernelX++) {
                    for (int kernelY = 0; kernelY < a; kernelY++) {
                        int x = mainX + kernelX - kernelRadius;
                        int y = mainY + kernelY - kernelRadius;

                        if(x >= 0 && x < vectorDim.width && y >= 0 && y < vectorDim.height)
                            sum += matrix[y * vectorDim.width + x] + kernel[kernelY * a + kernelX];
                    }
                }

                result[mainY * vectorDim.width + mainX] = sum;
            }
        }

        return result;
    }

    private static float safeFrom(float[] values, Dimension dim, int x, int y) {
        return (x < 0 || y < 0 || x >= dim.width || y >= dim.height) ? 0 : values[y * dim.width + x];
    }

    // from*from pixels (inputVector) -> to*to pixels (returned)
    public Vector elasticDeformation(Vector inputVector, Dimension from, Dimension to,
                                     int radius, float alpha, Random localRandom) {

        thr(from.height * from.width != inputVector.getNonBiasedLength());
        thr(!from.equals(to));
        int newlen = to.height * to.width;

        float[] input = inputVector.getValues();
        float[] result = new float[newlen + 1];
        result[newlen] = 1.0f;

        float[] diffX = new float[newlen];
        float[] diffY = new float[newlen];

        for (int i = 0; i < newlen; i++) {
            diffX[i] = (float) localRandom.nextGaussian();
            diffY[i] = (float) localRandom.nextGaussian();
        }

        diffX = gaussianFilter(diffX, to, radius);
        diffY = gaussianFilter(diffY, to, radius);

        for (int i = 0; i < newlen; i++) {
            float curX = (i % to.width) + diffX[i]*alpha;
            float curY = ((float) i) / to.width + diffY[i]*alpha;

            int xt0 = (int) Math.floor(curX), yt0 = (int) Math.floor(curY);
            int xt1 = xt0 + 1, yt1 = yt0 + 1;

            float op1 = safeFrom(input, from, xt0, yt0) * (xt1 - curX) * (yt1 - curY);
            float op2 = safeFrom(input, from, xt1, yt0) * (curX - xt0) * (yt1 - curY);
            float op3 = safeFrom(input, from, xt0, yt1) * (xt1 - curX) * (curY - yt0);
            float op4 = safeFrom(input, from, xt1, yt1) * (curX - xt0) * (curY - yt0);
            float resultValue = op1 + op2 + op3 + op4;

            if(resultValue > 1) {
                System.out.println(i + " " + resultValue);
                resultValue = 1;
            }
            result[i] = resultValue;
        }

//        System.out.println("result = " + Arrays.toString(result));
        return new Vector(true, result);
    }

    public int getSigmaRaduis() {
        return sigmaRaduis;
    }

    public float getAlphaMultiplier() {
        return alphaMultiplier;
    }
}
