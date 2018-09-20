package io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.binary;

import java.util.HashMap;
import java.util.Map;

import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.diff.DualNumber;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.diff.PartialDerivatives;

public class DivisionVertex extends DoubleBinaryOpVertex {
    /**
     * Divides one vertex by another
     *
     * @param left  the vertex to be divided
     * @param right the vertex to divide
     */
    public DivisionVertex(DoubleVertex left, DoubleVertex right) {
        super(left, right);
    }

    @Override
    protected DoubleTensor op(DoubleTensor l, DoubleTensor r) {
        return l.div(r);
    }

    @Override
    public Map<Vertex, PartialDerivatives> reverseModeAutoDifferentiation(PartialDerivatives derivativeOfOutputsWithRespectToSelf) {
        Map<Vertex, PartialDerivatives> partials = new HashMap<>();
        DoubleTensor leftValue = left.getValue();
        DoubleTensor rightValue = right.getValue();
        DoubleTensor dOutWrtLeft = rightValue.reciprocal();
        DoubleTensor dOutWrtRight = leftValue.div(rightValue.pow(2.0)).unaryMinusInPlace();
        partials.put(left, derivativeOfOutputsWithRespectToSelf.multiplyBy(dOutWrtLeft, true));
        partials.put(right, derivativeOfOutputsWithRespectToSelf.multiplyBy(dOutWrtRight, true));
        return partials;
    }

    @Override
    protected DualNumber dualOp(DualNumber l, DualNumber r) {
        return l.div(r);
    }
}
