package io.improbable.keanu.vertices.bool.nonprobabilistic.operators.unary;

import io.improbable.keanu.tensor.Tensor;
import io.improbable.keanu.tensor.TensorShapeValidation;
import io.improbable.keanu.tensor.bool.BooleanTensor;
import io.improbable.keanu.vertices.bool.BoolVertex;

public class BoolPluckVertex extends BoolUnaryOpVertex<BooleanTensor> {
    /**
     * A vertex that extracts a scalar at a given index
     *
     * @param inputVertex the input vertex to extract from
     * @param index the index to extract at
     */
    public BoolPluckVertex(BoolVertex inputVertex, int... index) {
        super(Tensor.SCALAR_SHAPE, inputVertex, a -> BooleanTensor.scalar(a.getValue(index)));
        TensorShapeValidation.checkIndexIsValid(inputVertex.getShape(), index);
    }
}
