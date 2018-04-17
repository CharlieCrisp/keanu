package io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.unary;

import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.ConstantDoubleVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.binary.PowerVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.UniformVertex;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CeilVertexTest {

    @Test
    public void canComputeCeilOperation() {
        DoubleVertex A = new ConstantDoubleVertex(8.12);
        CeilVertex ceil = new CeilVertex(A);

        assertEquals(Math.ceil(8.12), ceil.getValue(), 0.0001);
    }

    @Test
    public void canComputeCeilOperationWithValue() {
        CeilVertex ceil = new CeilVertex(8.12);

        assertEquals(Math.ceil(8.12), ceil.getValue(), 0.0001);
    }

    @Test
    public void ceilVertexDoesNotChangeDerivativeCalculations() {
        DoubleVertex A = new UniformVertex(0.0, 10.0);
        A.setValue(5.7);
        CeilVertex ceilVertex= new CeilVertex(A);
        PowerVertex powerVertex = new PowerVertex(ceilVertex, 2.0);

        assertEquals(2 * Math.pow(6.0, 2 - 1), powerVertex.getDualNumber().getInfinitesimal().getInfinitesimals().get(A.getId()), 0.000);
    }

}
