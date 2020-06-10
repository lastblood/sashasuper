package ru.sashasuper.utils;

public class Assertions {
    public static void thr(boolean condition) {
        if(condition)
            throw new IllegalArgumentException();
    }

    public static void thr(boolean condition, String message) {
        if(condition)
            throw new IllegalArgumentException(message);
    }
}
