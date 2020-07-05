package ru.sashasuper.logic.neural;

import org.junit.jupiter.api.Test;
import ru.sashasuper.logic.Matrix;
import ru.sashasuper.logic.Network;
import ru.sashasuper.logic.Vector;
import ru.sashasuper.logic.functions.Identity;
import ru.sashasuper.logic.functions.ReLU;

import static org.junit.jupiter.api.Assertions.*;
import static ru.sashasuper.logic.TestUtils.*;

public class ForwardPropagationTest {

    @Test
    void forwardIdentityLinear() {
        Network nn = new Network(new Matrix[]{ new Matrix(new float[][]{{2, -3, 0}}) },
                new Identity(), 0.1f);
        assertVectorsEquals(nn.process(new Vector(0, 1, 2)), -3.0f);
        assertVectorsEquals(nn.process(new Vector(10, -0.0f, -10)), 20);
        assertVectorsEquals(nn.process(new Vector(-10, -10, 50)), 10);
    }

    @Test
    void forwardReLULinear() {
        Network nn = new Network(new Matrix[]{ new Matrix(new float[][]{{2, -3, 0}}) },
                new ReLU(), 0.1f);

        assertVectorsEquals(nn.process(new Vector(0, 1, 2)), 0);
        assertVectorsEquals(nn.process(new Vector(10, -0.0f, -10)), 20);
        assertVectorsEquals(nn.process(new Vector(-10, -10, 50)), 10);

        Vector[] vs = nn.processUniversally(new Vector(0, 1, 2), true);
        assertEquals(2, vs.length);
        assertVectorsEquals(vs[0], new Vector(0,1,2));
        assertVectorsEquals(vs[1], 0);
    }

    @Test
    void forwardIdentityHidden() {
        Network nn = new Network(new Matrix[]{ new Matrix(new float[][]{{2, -3}, {0, 5}}), new Matrix(new float[][]{{-5, 2}}) },
                new Identity(), 0.1f);
        // -2 20
        assertVectorsEquals(nn.process(new Vector(5, 4)), 50);
    }

    @Test
    void forwardReLUHidden() {
        Network nn = new Network(new Matrix[]{ new Matrix(new float[][]{{2, -3}, {0, 5}}),
                                                new Matrix(new float[][]{{-5, 2}}) },
                new ReLU(), 0.1f);
        Vector input = new Vector(5, 4);
        Vector[] vectors = nn.processUniversally(new Vector(5, 4), true);

        assertEquals(3, vectors.length);

        assertVectorsEquals(input, vectors[0]);
        assertVectorsEquals(vectors[1], new Vector(0, 20));
        assertVectorsEquals(vectors[2], nn.process(input));
        assertVectorsEquals(vectors[2], 40);
    }
}
