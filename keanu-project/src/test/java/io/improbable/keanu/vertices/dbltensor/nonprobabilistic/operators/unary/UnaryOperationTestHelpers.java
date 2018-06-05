package io.improbable.keanu.vertices.dbltensor.nonprobabilistic.operators.unary;

import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.tensor.dbl.Nd4jDoubleTensor;
import io.improbable.keanu.vertices.dbltensor.DoubleVertex;
import io.improbable.keanu.vertices.dbltensor.nonprobabilistic.ConstantDoubleVertex;
import io.improbable.keanu.vertices.dbltensor.nonprobabilistic.diff.DualNumber;
import io.improbable.keanu.vertices.dbltensor.probabilistic.UniformVertex;

import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class UnaryOperationTestHelpers {

    public static void operatesOnScalarVertexValue(double aValue,
                                                   double expected,
                                                   Function<DoubleVertex, DoubleVertex> op) {

        ConstantDoubleVertex A = new ConstantDoubleVertex(aValue);

        assertEquals(expected, op.apply(A).getValue().scalar(), 1e-5);
    }

    public static void calculatesDualNumberOfScalar(double aValue,
                                                    double expectedGradientWrtA,
                                                    Function<DoubleVertex, DoubleVertex> op) {

        UniformVertex A = new UniformVertex(0.0, 1.0);
        A.setAndCascade(Nd4jDoubleTensor.scalar(aValue));

        DualNumber resultDualNumber = op.apply(A).getDualNumber();
        assertEquals(expectedGradientWrtA, resultDualNumber.getPartialDerivatives().withRespectTo(A).scalar(), 1e-5);
    }

    public static void operatesOn2x2MatrixVertexValues(double[] aValues,
                                                       double[] expected,
                                                       Function<DoubleVertex, DoubleVertex> op) {

        ConstantDoubleVertex A = new ConstantDoubleVertex(Nd4jDoubleTensor.create(aValues, new int[]{2, 2}));

        DoubleTensor result = op.apply(A).getValue();

        DoubleTensor expectedTensor = Nd4jDoubleTensor.create(expected, new int[]{2, 2});

        assertEquals(expectedTensor.getValue(0, 0), result.getValue(0, 0), 1e-5);
        assertEquals(expectedTensor.getValue(0, 1), result.getValue(0, 1), 1e-5);
        assertEquals(expectedTensor.getValue(1, 0), result.getValue(1, 0), 1e-5);
        assertEquals(expectedTensor.getValue(1, 1), result.getValue(1, 1), 1e-5);
    }

    public static void calculatesDualNumberOfMatrixElementWiseOperator(double[] aValues,
                                                                       double[] expectedGradientWrtA,
                                                                       Function<DoubleVertex, DoubleVertex> op) {

        UniformVertex A = new UniformVertex(new int[]{2, 2}, new ConstantDoubleVertex(0.0), new ConstantDoubleVertex(1.0));
        A.setAndCascade(Nd4jDoubleTensor.create(aValues, new int[]{2, 2}));

        DualNumber result = op.apply(A).getDualNumber();
        DoubleTensor expectedTensorA = Nd4jDoubleTensor.create(expectedGradientWrtA, new int[]{2, 2});

        DoubleTensor wrtA = result.getPartialDerivatives().withRespectTo(A);
        assertEquals(expectedTensorA.getValue(0, 0), wrtA.getValue(0, 0), 1e-5);
        assertEquals(expectedTensorA.getValue(0, 1), wrtA.getValue(0, 1), 1e-5);
        assertEquals(expectedTensorA.getValue(1, 0), wrtA.getValue(1, 0), 1e-5);
        assertEquals(expectedTensorA.getValue(1, 1), wrtA.getValue(1, 1), 1e-5);
    }
}
