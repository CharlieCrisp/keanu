package io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.binary;

import static io.improbable.keanu.tensor.TensorShapeValidation.checkHasSingleNonScalarShapeOrAllScalar;

import java.util.Map;

import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.dbl.Differentiable;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.diff.DualNumber;

public class DivisionVertex extends DoubleBinaryOpVertex {
    /**
     * Divides one vertex by another
     *
     * @param left the vertex to be divided
     * @param right the vertex to divide
     */
    public DivisionVertex(DoubleVertex left, DoubleVertex right) {
        super(checkHasSingleNonScalarShapeOrAllScalar(left.getShape(), right.getShape()), left, right);
    }

    public DoubleVertex getDividend(){
        return super.getLeft();
    }

    public DoubleVertex getDivsor(){
        return super.getRight();
    }

    @Override
    public DualNumber calculateDualNumber(Map<Differentiable, DualNumber> dualNumbers) {
        DualNumber leftDual = dualNumbers.get(left);
        DualNumber rightDual = dualNumbers.get(right);
        return leftDual.divideBy(rightDual);
    }

    @Override
    protected DoubleTensor op(DoubleTensor left, DoubleTensor right) {
        return left.div(right);
    }
}
