package io.improbable.keanu.vertices;

import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.diff.PartialDerivatives;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.binary.DoubleBinaryOpVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.unary.DoubleUnaryOpVertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class TestGraphGenerator {

    private static final Logger log = LoggerFactory.getLogger(TestGraphGenerator.class);

    static DoubleVertex addLinks(DoubleVertex end, AtomicInteger opCount, AtomicInteger autoDiffCount, int links) {

        for (int i = 0; i < links; i++) {
            DoubleVertex left = passThroughVertex(end, opCount, autoDiffCount, id -> log.info("OP on id:" + id));
            DoubleVertex right = passThroughVertex(end, opCount, autoDiffCount, id -> log.info("OP on id:" + id));
            end = sumVertex(left, right, opCount, autoDiffCount, id -> log.info("OP on id:" + id));
        }

        return end;
    }

    static class PassThroughVertex extends DoubleUnaryOpVertex {

        private final AtomicInteger opCount;
        private final AtomicInteger autoDiffCount;
        private final Consumer<VertexId> onOp;

        public PassThroughVertex(DoubleVertex inputVertex, AtomicInteger opCount, AtomicInteger autoDiffCount, Consumer<VertexId> onOp) {
            super(inputVertex);
            this.opCount = opCount;
            this.autoDiffCount = autoDiffCount;
            this.onOp = onOp;
        }

        @Override
        protected DoubleTensor op(DoubleTensor a) {
            opCount.incrementAndGet();
            onOp.accept(getId());
            return a;
        }

        @Override
        protected PartialDerivatives forwardModeAutoDifferentiation(PartialDerivatives derivativeOfParentWithRespectToInputs) {
            autoDiffCount.incrementAndGet();
            return derivativeOfParentWithRespectToInputs;
        }
    }

    static DoubleVertex passThroughVertex(DoubleVertex from, AtomicInteger opCount, AtomicInteger autoDiffCount, Consumer<VertexId> onOp) {
        return new PassThroughVertex(from, opCount, autoDiffCount, onOp);
    }

    static class SumVertex extends DoubleBinaryOpVertex {

        private final AtomicInteger opCount;
        private final AtomicInteger autoDiffCount;
        private final Consumer<VertexId> onOp;

        public SumVertex(DoubleVertex left, DoubleVertex right,
                         AtomicInteger opCount, AtomicInteger autoDiffCount,
                         Consumer<VertexId> onOp) {
            super(left, right);
            this.opCount = opCount;
            this.autoDiffCount = autoDiffCount;
            this.onOp = onOp;
        }

        @Override
        protected DoubleTensor op(DoubleTensor l, DoubleTensor r) {
            opCount.incrementAndGet();
            onOp.accept(getId());
            return l.plus(r);
        }

        @Override
        protected PartialDerivatives forwardModeAutoDifferentiation(PartialDerivatives l, PartialDerivatives r) {
            autoDiffCount.incrementAndGet();
            return l.add(r);
        }
    }

    static DoubleVertex sumVertex(DoubleVertex left, DoubleVertex right, AtomicInteger opCount, AtomicInteger dualNumberCount, Consumer<VertexId> onOp) {
        return new SumVertex(left, right, opCount, dualNumberCount, onOp);
    }

}
