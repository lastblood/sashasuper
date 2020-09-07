package ru.sashasuper.logic.functions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ActivateFunctionTest {

    @Test
    void logisticTest() {
        Logistic func = new Logistic();
        float result = 0.95257413f;
        float res = func.process(3);
        assertEquals(res, result);

        result = 0.18242553f;
        res = func.process((float) -1.5);
        assertEquals(res, result);

        result = 0.104993574f;
        res = func.derivative(2);
        assertEquals(res, result);
    }

    @Test
    void tanHTest() {
        TanH func = new TanH();
        float result = 0.9999092f;
        float res = func.process(5);
        assertEquals(res, result);

        result = -0.9640276f;
        res = func.process(-2);
        assertEquals(result, res);

        result = 0.070650816f;
        res = func.derivative(2);
        assertEquals(result, res);
    }
/*
    @Test
    void reluTest() {
        ReLU func = new ReLU();
        float result = 3;
        float res = func.process(3);
        assertEquals(res,result);

        result = 1;
        res = func.derivative(7);
        assertEquals(res, result);

    }
*/
}