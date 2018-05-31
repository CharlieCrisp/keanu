package io.improbable.keanu.vertices.generictensor.nonprobabilistic;

import io.improbable.keanu.tensor.Tensor;
import io.improbable.keanu.tensor.TensorShape;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbltensor.KeanuRandom;
import io.improbable.keanu.vertices.intgrtensor.IntegerVertex;

public class MultiplexerVertex<T, TENSOR extends Tensor<T>> extends NonProbabilistic<T, TENSOR> {

    private final IntegerVertex selectorControlVertex;
    private final Vertex<TENSOR>[] selectVertices;

    public MultiplexerVertex(IntegerVertex selectorControlVertex, Vertex<TENSOR>... select) {

        if (!TensorShape.isScalar(selectorControlVertex.getShape())) {
            throw new IllegalArgumentException("Select control must be scalar integer");
        }

        this.selectVertices = select;
        this.selectorControlVertex = selectorControlVertex;
        setParents(select);
        addParent(selectorControlVertex);
    }

    @Override
    public TENSOR sample(KeanuRandom random) {
        return getDerivedValue();
    }

    @Override
    public TENSOR getDerivedValue() {
        Vertex<TENSOR> selector = getSelector();
        return selector.getValue();
    }

    private Vertex<TENSOR> getSelector() {
        int optionGroupIdx = selectorControlVertex.getValue().scalar();
        return selectVertices[optionGroupIdx];
    }
}