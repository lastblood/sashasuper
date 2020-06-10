package ru.sashasuper.logic;

import java.io.Serializable;

import static ru.sashasuper.utils.Assertions.thr;

public class Vector implements Serializable {
    private int len;
    private float[] values;

    public Vector(int len, float[] values) {
        thr(len != values.length);
        this.len = len;
        this.values = values;
    }

    private Vector() {
    }

    public int getLen() {
        return len;
    }

    public float[] getValues() {
        return values;
    }
}


