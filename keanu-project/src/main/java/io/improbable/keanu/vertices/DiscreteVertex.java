package io.improbable.keanu.vertices;

import java.util.Map;

public abstract class DiscreteVertex<T> extends Vertex<T> {

    @Override
    public final double logProb(T value) {
        return logPmf(value);
    }

    public final Map<String, Double> dLogProb(T value) {
        return dLogPmf(value);
    }

    public final Map<String, Double> dLogProbAtValue() {
        return dLogProb(getValue());
    }

    public abstract double logPmf(T value);

    public abstract Map<String, Double> dLogPmf(T value);

}
