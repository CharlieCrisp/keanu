package io.improbable.keanu.vertices.booltensor.nonprobabilistic;

import io.improbable.keanu.tensor.bool.BooleanTensor;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbltensor.KeanuRandom;

public class BooleanIfVertex extends NonProbabilisticBool {

    private final Vertex<? extends BooleanTensor> predicate;
    private final Vertex<? extends BooleanTensor> thn;
    private final Vertex<? extends BooleanTensor> els;

    public BooleanIfVertex(Vertex<? extends BooleanTensor> predicate,
                           Vertex<? extends BooleanTensor> thn,
                           Vertex<? extends BooleanTensor> els) {
        this.predicate = predicate;
        this.thn = thn;
        this.els = els;
        setParents(predicate, thn, els);
    }

    protected BooleanTensor op(BooleanTensor predicate, BooleanTensor thn, BooleanTensor els) {
        return predicate.setBooleanIf(thn, els);
    }

    @Override
    public BooleanTensor sample(KeanuRandom random) {
        return op(predicate.sample(random), thn.sample(random), els.sample(random));
    }

    @Override
    public BooleanTensor getDerivedValue() {
        return op(predicate.getValue(), thn.getValue(), els.getValue());
    }
}
