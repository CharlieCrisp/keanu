package io.improbable.keanu.vertices.dbltensor.nonprobabilistic.operators.unary;

import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbltensor.DoubleVertex;
import io.improbable.keanu.vertices.dbltensor.nonprobabilistic.diff.DualNumber;

import java.util.Map;

public class CeilVertex extends DoubleUnaryOpVertex {

    public CeilVertex(DoubleVertex inputVertex) {
        super(inputVertex.getShape(), inputVertex);
    }

    @Override
    protected DoubleTensor op(DoubleTensor a) {
        return a.ceil();
    }

    @Override
    protected DualNumber calculateDualNumber(Map<Vertex, DualNumber> dualNumbers) {
        throw new UnsupportedOperationException();
    }
}
