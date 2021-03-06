package io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.binary;

import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import org.junit.Test;

import static io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.binary.BinaryOperationTestHelpers.*;

public class DifferenceVertexTest {

    @Test
    public void subtractsTwoScalarVertexValues() {
        operatesOnTwoScalarVertexValues(2.0, 3.0, -1, DoubleVertex::minus);
    }

    @Test
    public void calculatesDerivativeOfTwoScalarsSubtracted() {
        calculatesDerivativeOfTwoScalars(2.0, 3.0, 1.0, -1.0, DoubleVertex::minus);
    }

    @Test
    public void subtractsTwoMatrixVertexValues() {
        operatesOnTwo2x2MatrixVertexValues(
            new double[]{1.0, 2.0, 6.0, 4.0},
            new double[]{2.0, 4.0, 3.0, 8.0},
            new double[]{-1.0, -2.0, 3.0, -4.0},
            DoubleVertex::minus
        );
    }

    @Test
    public void calculatesDerivativeOfTwoMatricesElementWiseSubtracted() {
        calculatesDerivativeOfTwoMatricesElementWiseOperator(
            DoubleTensor.create(new double[]{1.0, 2.0, 3.0, 4.0}, 1, 4),
            DoubleTensor.create(new double[]{2.0, 3.0, 4.0, 5.0}, 1, 4),
            DoubleTensor.create(new double[]{1.0, 1.0, 1.0, 1.0}).diag().reshape(1, 4, 1, 4),
            DoubleTensor.create(new double[]{-1.0, -1.0, -1.0, -1.0}).diag().reshape(1, 4, 1, 4),
            DoubleVertex::minus
        );
    }

    @Test
    public void calculatesDerivativeOfAVectorsAndScalarSubtracted() {
        calculatesDerivativeOfAVectorAndScalar(
            DoubleTensor.create(new double[]{1.0, 2.0, 3.0, 4.0}),
            2,
            DoubleTensor.eye(4).reshape(1, 4, 1, 4),
            DoubleTensor.ones(1, 4, 1, 1).unaryMinus(),
            DoubleVertex::minus
        );
    }

    @Test
    public void calculatesDerivativeofAScalarAndVectorSubtracted() {
        calculatesDerivativeOfAScalarAndVector(
            2,
            DoubleTensor.create(new double[]{1.0, 2.0, 3.0, 4.0}),
            DoubleTensor.ones(1, 4, 1, 1),
            DoubleTensor.eye(4).reshape(1, 4, 1, 4).unaryMinus(),
            DoubleVertex::minus
        );
    }
}
