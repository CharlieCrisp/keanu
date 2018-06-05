package io.improbable.keanu.vertices.dbltensor.nonprobabilistic.operators.unary;

import io.improbable.keanu.vertices.dbltensor.DoubleTensorVertex;
import org.junit.Test;

import static io.improbable.keanu.vertices.dbltensor.nonprobabilistic.operators.unary.UnaryOperationTestHelpers.*;

public class TensorArcCosVertexTest {

    @Test
    public void acosScalarVertexValue() {
        operatesOnScalarVertexValue(
            Math.PI,
            Math.acos(Math.PI),
            DoubleTensorVertex::acos
        );
    }

    @Test
    public void calculatesDualNumberOScalarACos() {
        calculatesDualNumberOfScalar(
            0.5,
            -1.0 / Math.sqrt(1.0 - 0.5 * 0.5),
            DoubleTensorVertex::acos
        );
    }

    @Test
    public void acosMatrixVertexValues() {
        operatesOn2x2MatrixVertexValues(
            new double[]{0.0, 0.1, 0.2, 0.3},
            new double[]{Math.acos(0.0), Math.acos(0.1), Math.acos(0.2), Math.acos(0.3)},
            DoubleTensorVertex::acos
        );
    }

    @Test
    public void calculatesDualNumberOfMatrixElementWiseACos() {
        calculatesDualNumberOfMatrixElementWiseOperator(
            new double[]{0.1, 0.2, 0.3, 0.4},
            new double[]{-1.0 / Math.sqrt(1.0 - 0.1 * 0.1),
                -1.0 / Math.sqrt(1.0 - 0.2 * 0.2),
                -1.0 / Math.sqrt(1.0 - 0.3 * 0.3),
                -1.0 / Math.sqrt(1.0 - 0.4 * 0.4)
            },
            DoubleTensorVertex::acos
        );
    }

}
