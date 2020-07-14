package ru.sashasuper.logic;

import ru.sashasuper.logic.functions.ActivateFunction;
import ru.sashasuper.logic.functions.ReLU;
import ru.sashasuper.utils.NanDefender;

import java.io.Serializable;
import java.util.*;

import static ru.sashasuper.logic.VectorMath.*;
import static ru.sashasuper.utils.Assertions.thr;

public class Network implements Serializable, Cloneable {
    private int inputSize;
    private int outputSize;
    private int layerCount;

    private Matrix[] weightMatrices;
    private ActivateFunction activateFunction;
    private float learningRate;
    private boolean withBias;

    public Network(Matrix[] weightMatrices, ActivateFunction activateFunction, float learningRate, boolean withBias) {
        thr(weightMatrices == null);
        thr(learningRate <= 0 || learningRate > 1);

        this.inputSize = weightMatrices[0].getColumns();
        this.outputSize = weightMatrices[weightMatrices.length - 1].getRows();
        this.layerCount = weightMatrices.length - 1;

        this.weightMatrices = weightMatrices;
        this.activateFunction = activateFunction;
        this.learningRate = learningRate;
        this.withBias = withBias;
    }

    public Network(Matrix[] weightMatrices, ActivateFunction activateFunction, float learningRate) {
        thr(weightMatrices == null);
        thr(learningRate <= 0 || learningRate > 1);

        this.inputSize = weightMatrices[0].getColumns();
        this.outputSize = weightMatrices[weightMatrices.length - 1].getRows();
        this.layerCount = weightMatrices.length - 1;

        this.weightMatrices = weightMatrices;
        this.activateFunction = activateFunction;
        this.learningRate = learningRate;

        this.withBias = true;
    }

    public int getInputSize() {
        return inputSize;
    }

    public int getOutputSize() {
        return outputSize;
    }

    public int getHiddenLayerCount() {
        return layerCount;
    }

    public Matrix[] getWeightMatrices() {
        return weightMatrices;
    }

    public ActivateFunction getActivateFunction() {
        return activateFunction;
    }

    public float getLearningRate() {
        return learningRate;
    }

    public void setLearningRate(float learningRate) {
        this.learningRate = learningRate;
    }

    // Всегда воспринимает, как non-biased input
    public Vector process(float ... values) {
        return process(new Vector(values));
    }

    public Vector process(boolean biased, float ... values) {
        return process(new Vector(biased, values));
    }

    public Vector process(Vector input) {
        thr(getWeightMatrices()[0].getColumns() != input.getLength(withBias));

        Vector currentVector = input;
        for (int i = 0; i < getWeightMatrices().length; i++) {
            currentVector = multMatrixVector(getWeightMatrices()[i], currentVector, withBias);
            currentVector = applyToVector(currentVector, getActivateFunction());
        }

        return currentVector;
    }

    public Vector[] processRemember(Vector input) {
        thr(getWeightMatrices()[0].getColumns() != input.getLength(withBias));
        thr(NanDefender.inVector(input));

        Vector[] memories = new Vector[getHiddenLayerCount() + 2];
        memories[0] = input;

        Vector currentVector = input;
        for (int i = 0; i < getWeightMatrices().length; i++) {
            // todo: оптимизировать матричные операции для создания вектора сразу с bias
            currentVector = multMatrixVector(getWeightMatrices()[i], currentVector, withBias);
            currentVector = applyToVector(currentVector, getActivateFunction());
            memories[i + 1] = currentVector;
        }

        return memories;
    }

    // Используется только вычитание двух векторов (И ТОЛЬКО ЗДЕСЬ)
    public void backPropagation(Vector input, Vector expectedOutput) {
        // Прогнать прямое распространение и запомнить результаты
        Vector[] vectors = processRemember(input);
        thr(!Arrays.stream(vectors).allMatch(x -> x.getValues()[x.getValues().length - 1] == 1));

        thr(vectors.length != getHiddenLayerCount() + 2);
//        System.out.println("vectors = " + Arrays.toString(vectors));

        // Найти ошибку для выходного вектора
        Vector error_layer = subElements(vectors[getHiddenLayerCount() + 1], expectedOutput, withBias);
        thr(NanDefender.inVector(error_layer));

        thr(!Arrays.stream(vectors).allMatch(x -> x.getValues()[x.getValues().length - 1] == 1));

        // Прогнать через backPropagation все слои
        for (int index = getWeightMatrices().length - 1; index >= 0; index--) {
            thr(!Arrays.stream(vectors).allMatch(x -> x.getValues()[x.getValues().length - 1] == 1));
            error_layer = backPropagationIter(vectors, error_layer, index);
            thr(NanDefender.inVector(error_layer));
        }
    }

    // Перемножение вектора-строки и вектора-столбца, транспонирование матрицы, умножение матрицы на число,
    //  поэлементное вычитание матриц, применение матрицы к вектору с другой стороны (ВСЕ ЭТО ТОЛЬКО ЗДЕСЬ)
    
    // ATTENTION!!! НЕ ПОТОКО-БЕЗОПАСНОЕ, МЕНЯЕТ МАТРИЦЫ ВНУТРИ network
    // currentIndex указывает на индекс текущей меняемой матрицы
    private Vector backPropagationIter(Vector[] networkState, Vector error_layer, int currentIndex) {
//        System.out.println("backPropagationIter():\nerror_layer = " + error_layer);
        thr(currentIndex < 0 || currentIndex >= weightMatrices.length);

        Vector currentLayer = networkState[currentIndex + 1];
        Vector lastLayer = networkState[currentIndex];

        Vector gradient_layer = applyToVector(currentLayer, activateFunction, true);
        // Очевидно, для identity gradient_layer всегда будет равен единице
        thr(NanDefender.inVector(gradient_layer));

        // Вектор дельты
        Vector delta_layer = multElements(error_layer, gradient_layer, withBias);
//        System.out.println("delta_layer = " + delta_layer);
        thr(NanDefender.inVector(delta_layer));

        //todo: можно реализовать смешанную операцию: умножение двух векторов

//БЫЛО
//        Matrix temp = multVectors(delta_layer, lastLayer, withBias);
        Matrix temp = multVectors(lastLayer, delta_layer, withBias);
//        System.out.println("temp = " + temp);

        // Корректировочная матрица для текущей матрицы весов в Network
        Matrix subMatrix = multMatrixByT(temp, getLearningRate());
//        System.out.println("subMatrix = " + subMatrix);

        Matrix current = getWeightMatrices()[currentIndex];
//        System.out.println("current = " + current);
        thr(subMatrix.getColumns() != current.getColumns() || subMatrix.getRows() != current.getRows());

        // Корректируем текущую матрицу весов
        getWeightMatrices()[currentIndex] = subMatrices(current, subMatrix);

        // Отправляем ошибку на уровень назад
        Vector result = multMatrixVectorTransposed(getWeightMatrices()[currentIndex], delta_layer, withBias);
        thr(NanDefender.inVector(result));
        return result;
    }

    private static int getMaxIndex(Vector v) {
        int max = 0;
        for (int i = 1; i < v.getValues().length - 1; i++) {
            if(v.getValues()[i] > v.getValues()[max]) {
                max = i;
            }
        }
        return max;
    }

    public MomentumStat test(Dataset dataset) {
        float min = Float.MAX_VALUE, max = Float.MIN_VALUE;
        double resultError = 0.0;
        int right = 0, wrong = 0;

        List<AbstractMap.SimpleEntry<Vector, Vector>> getAll = new ArrayList<>(dataset.getAll());
        Collections.shuffle(getAll);

        List<AbstractMap.SimpleEntry<Vector, Vector>> bad = new ArrayList<>(getAll.size() / 10);

        for (AbstractMap.SimpleEntry<Vector, Vector> entry : getAll) {
            Vector resultVector = process(entry.getKey());

            int ind1 = getMaxIndex(entry.getValue()), ind2 = getMaxIndex(resultVector);
            thr(ind1 < 0 || ind1 > 9 || ind2 < 0 || ind2 > 9);

            float mse = VectorMath.MSE(resultVector, entry.getValue());

            if(ind1 == ind2)
                right++;
            else
                wrong++;

            if(ind1 != ind2 || mse > 0.7f)
                bad.add(entry);

            min = Float.min(mse, min);
            max = Float.max(mse, max);

            resultError += mse;
        }

        MomentumStat momentumStat = new MomentumStat(right, wrong, min, max, resultError);
        Collections.shuffle(bad);
        momentumStat.bad = bad;
        return momentumStat;
    }

    public void learnIteration(Dataset dataset) {
        for (AbstractMap.SimpleEntry<Vector, Vector> entry : dataset.getAll()) {
            backPropagation(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Network c = (Network) super.clone();

        Matrix[] matrices = new Matrix[weightMatrices.length];
        for (int i = 0; i < matrices.length; i++) {
            matrices[i] = (Matrix) getWeightMatrices()[i].clone();
        }
        c.weightMatrices = matrices;

        return c;
    }
}
