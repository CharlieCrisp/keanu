package io.improbable.keanu.vertices.dbltensor.nonprobabilistic.operators.unary;

import io.improbable.keanu.vertices.dbltensor.DoubleTensorVertex;
import org.junit.Test;

import static io.improbable.keanu.vertices.dbltensor.nonprobabilistic.operators.unary.UnaryOperationTestHelpers.*;

public class TensorCosVertexTest {

    @Test
    public void cosScalarVertexValue() {
        operatesOnScalarVertexValue(
            Math.PI,
            Math.cos(Math.PI),
            DoubleTensorVertex::cos
        );
    }

    @Test
    public void calculatesDualNumberOScalarCos() {
        calculatesDualNumberOfScalar(
            0.5,
            -Math.sin(0.5),
            DoubleTensorVertex::cos
        );
    }

    @Test
    public void cosMatrixVertexValues() {
        operatesOn2x2MatrixVertexValues(
            new double[]{0.0, 0.1, 0.2, 0.3},
            new double[]{Math.cos(0.0), Math.cos(0.1), Math.cos(0.2), Math.cos(0.3)},
            DoubleTensorVertex::cos
        );
    }

    @Test
    public void calculatesDualNumberOfMatrixElementWiseCos() {
        calculatesDualNumberOfMatrixElementWiseOperator(
            new double[]{0.1, 0.2, 0.3, 0.4},
            new double[]{-Math.sin(0.1), -Math.sin(0.2), -Math.sin(0.3), -Math.sin(0.4)},
            DoubleTensorVertex::cos
        );
    }

}
