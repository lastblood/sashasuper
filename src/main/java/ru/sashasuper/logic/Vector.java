package ru.sashasuper.logic;

import java.io.Serializable;

public class Vector implements Serializable {
    private int len;
    private float[] values;

    public Vector(int len, float[] values) {
        this.len = len;
        this.values = values;
    }

    public Vector() {
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public float[] getValues() {
        return values;
    }

    public void setValues(float[] values) {
        this.values = values;
    }
}


