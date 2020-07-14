package ru.sashasuper.utils;

import ru.sashasuper.logic.neural.*;

import java.util.Arrays;

public class NanDefender {
    public static boolean inVector(Vector v) {
        float[] arr = v.getValues();
        for (float x : arr)
            if(x != x) return true;
        return false;
    }

    public static boolean inMatrix(Matrix m) {
        return Arrays.stream(m.getValues()).anyMatch(arr -> {
            for (float v : arr)
                if(v != v) return true;
            return false;
        });
    }

    public static boolean inNetwork(Network n) {
        return Arrays.stream(n.getWeightMatrices()).anyMatch(NanDefender::inMatrix);
    }
}
