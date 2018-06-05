package io.improbable.keanu.vertices.dbltensor.nonprobabilistic.operators.unary;

import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.dbltensor.DoubleVertex;
import io.improbable.keanu.vertices.dbltensor.KeanuRandom;
import io.improbable.keanu.vertices.dbltensor.nonprobabilistic.NonProbabilisticDouble;

public abstract class DoubleUnaryOpVertex extends NonProbabilisticDouble {

    protected final DoubleVertex inputVertex;

    public DoubleUnaryOpVertex(int[] shape, DoubleVertex inputVertex) {
        this.inputVertex = inputVertex;
        setParents(inputVertex);
        setValue(DoubleTensor.placeHolder(shape));
    }

    @Override
    public DoubleTensor sample(KeanuRandom random) {
        return op(inputVertex.sample(random));
    }

    @Override
    public DoubleTensor getDerivedValue() {
        return op(inputVertex.getValue());
    }

    protected abstract DoubleTensor op(DoubleTensor a);

}
