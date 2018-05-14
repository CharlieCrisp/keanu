package io.improbable.keanu.vertices.intgr.probabilistic;

import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbltensor.DoubleTensor;
import io.improbable.keanu.vertices.intgr.nonprobabilistic.ConstantIntegerVertex;

import java.util.Map;
import java.util.Random;

public class UniformIntVertex extends ProbabilisticInteger {

    private Vertex<Integer> min;
    private Vertex<Integer> max;
    private Random random;

    /**
     * @param min    The inclusive lower bound.
     * @param max    The exclusive upper bound.
     * @param random source of randomness
     */
    public UniformIntVertex(Vertex<Integer> min, Vertex<Integer> max, Random random) {
        this.min = min;
        this.max = max;
        this.random = random;
        setParents(min, max);
    }

    public UniformIntVertex(int min, int max, Random random) {
        this(new ConstantIntegerVertex(min), new ConstantIntegerVertex(max), random);
    }

    public UniformIntVertex(Vertex<Integer> min, Vertex<Integer> max) {
        this(min, max, new Random());
    }

    public UniformIntVertex(Vertex<Integer> min, int max) {
        this(min, new ConstantIntegerVertex(max), new Random());
    }

    public UniformIntVertex(int min, Vertex<Integer> max) {
        this(new ConstantIntegerVertex(min), max, new Random());
    }

    public UniformIntVertex(int min, int max) {
        this(new ConstantIntegerVertex(min), new ConstantIntegerVertex(max));
    }

    public Vertex<Integer> getMin() {
        return min;
    }

    public Vertex<Integer> getMax() {
        return max;
    }

    @Override
    public double logPmf(Integer value) {
        final double probability = 1.0 / (max.getValue() - min.getValue());
        return Math.log(probability);
    }

    @Override
    public Map<String, DoubleTensor> dLogPmf(Integer value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Integer sample(Random random) {
        return min.getValue() + random.nextInt(max.getValue() - min.getValue());
    }
}
