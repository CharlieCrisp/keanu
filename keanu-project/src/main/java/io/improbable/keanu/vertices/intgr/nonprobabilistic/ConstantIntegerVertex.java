package io.improbable.keanu.vertices.intgr.nonprobabilistic;

import java.util.Map;

import io.improbable.keanu.tensor.intgr.IntegerTensor;
import io.improbable.keanu.vertices.dbl.Differentiable;
import io.improbable.keanu.vertices.dbl.KeanuRandom;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.diff.DualNumber;

public class ConstantIntegerVertex extends NonProbabilisticInteger implements Differentiable {

    public ConstantIntegerVertex(IntegerTensor constant) {
        setValue(constant);
    }

    public ConstantIntegerVertex(int[] vector) {
        this(IntegerTensor.create(vector));
    }

    public ConstantIntegerVertex(int constant) {
        this(IntegerTensor.scalar(constant));
    }

    @Override
    public IntegerTensor sample(KeanuRandom random) {
        return getValue();
    }

    @Override
    public IntegerTensor getDerivedValue() {
        return getValue();
    }

    @Override
    public DualNumber calculateDualNumber(Map<Differentiable, DualNumber> dualNumbers) {
        return DualNumber.createConstant(getValue().toDouble());
    }
}
