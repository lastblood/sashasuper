package ru.sashasuper.logic.neural;

import org.junit.jupiter.api.Test;
import ru.sashasuper.logic.Matrix;
import ru.sashasuper.logic.Network;
import ru.sashasuper.logic.Vector;
import ru.sashasuper.logic.functions.Identity;
import ru.sashasuper.logic.functions.ReLU;

import static ru.sashasuper.logic.TestUtils.assertVectorsEquals;

public class ForwardPropagationBiasedTest {
    @Test
    void forwardIdentityLinear() {
        Network nn = new Network(new Matrix[]{ new Matrix(new float[][]{{2, -3, 0, -2}}) },
                new Identity(), 0.1f, true);
        assertVectorsEquals(nn.process(new Vector(0, 1, 2)), -5.0f);
        assertVectorsEquals(nn.process(new Vector(10, -0.0f, -10)), 18);
        assertVectorsEquals(nn.process(new Vector(-10, -10, 50)), 8);
    }

    @Test
    void forwardTwoReLU() {
        Network nn = new Network(new Matrix[]{
                        new Matrix(new float[][]{{2, 2, 2}, {1, 1, -1}}),
                        new Matrix(new float[][]{{0.5f, 3, 0}})},
                new ReLU(), 0.5f, true);

        assertVectorsEquals(nn.process(new Vector(5, 3)), 30);
        assertVectorsEquals(nn.process(new Vector(-2.5f, 4.5f)), 6);
    }
}