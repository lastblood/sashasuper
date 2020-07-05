package ru.sashasuper.logic;

import java.io.Serializable;
import java.util.Arrays;

import static ru.sashasuper.utils.Assertions.thr;

public class Vector implements Serializable {
    private float[] values;

    public Vector(float ... values) {
        thr(values.length == 0);
        this.values = values;
    }

    private Vector() {
    }

    public int getLen() {
        return values.length;
    }

    public float[] getValues() {
        return values;
    }

    @Override
    public String toString() {
        return Arrays.toString(values);
    }
}


