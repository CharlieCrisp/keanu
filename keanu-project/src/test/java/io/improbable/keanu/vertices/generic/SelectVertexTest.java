package io.improbable.keanu.vertices.generic;

import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.ConstantDoubleVertex;
import io.improbable.keanu.vertices.generic.probabilistic.discrete.SelectVertex;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class SelectVertexTest {
    private final Logger log = LoggerFactory.getLogger(SelectVertexTest.class);

    private static double epsilon = 0.01;
    private static int N = 100000;

    private Random random;

    @Before
    public void setup(){
        random = new Random(1);
    }

    @Test
    public void fourValuesEquallyWeightedSummingToOne() {

        LinkedHashMap<TestEnum, DoubleVertex> selectableValues = new LinkedHashMap<>();
        selectableValues.put(TestEnum.A, new ConstantDoubleVertex(0.25));
        selectableValues.put(TestEnum.B, new ConstantDoubleVertex(0.25));
        selectableValues.put(TestEnum.C, new ConstantDoubleVertex(0.25));
        selectableValues.put(TestEnum.D, new ConstantDoubleVertex(0.25));

        LinkedHashMap<TestEnum, Double> proportions = testSample(selectableValues, random);
        assertProportionsWithinExpectedRanges(selectableValues, proportions);
    }

    @Test
    public void fourValuesNotEquallyWeightedSummingToOne() {

        LinkedHashMap<TestEnum, DoubleVertex> selectableValues = new LinkedHashMap<>();
        selectableValues.put(TestEnum.A, new ConstantDoubleVertex(0.1));
        selectableValues.put(TestEnum.B, new ConstantDoubleVertex(0.2));
        selectableValues.put(TestEnum.C, new ConstantDoubleVertex(0.3));
        selectableValues.put(TestEnum.D, new ConstantDoubleVertex(0.4));

        LinkedHashMap<TestEnum, Double> proportions = testSample(selectableValues, random);
        assertProportionsWithinExpectedRanges(selectableValues, proportions);
    }

    @Test
    public void fourValuesEquallyWeightedSummingToFour() {

        LinkedHashMap<TestEnum, DoubleVertex> selectableValues = new LinkedHashMap<>();
        selectableValues.put(TestEnum.A, new ConstantDoubleVertex(1.0));
        selectableValues.put(TestEnum.B, new ConstantDoubleVertex(1.0));
        selectableValues.put(TestEnum.C, new ConstantDoubleVertex(1.0));
        selectableValues.put(TestEnum.D, new ConstantDoubleVertex(1.0));

        LinkedHashMap<TestEnum, Double> proportions = testSample(selectableValues, random);
        LinkedHashMap<TestEnum, DoubleVertex> normalisedSelectableValues = normaliseSelectableValues(selectableValues, 4.0);
        assertProportionsWithinExpectedRanges(normalisedSelectableValues, proportions);
    }

    @Test
    public void fourValuesNotEquallyWeightedSummingToFour() {

        LinkedHashMap<TestEnum, DoubleVertex> selectableValues = new LinkedHashMap<>();
        selectableValues.put(TestEnum.A, new ConstantDoubleVertex(0.25));
        selectableValues.put(TestEnum.B, new ConstantDoubleVertex(0.75));
        selectableValues.put(TestEnum.C, new ConstantDoubleVertex(1.25));
        selectableValues.put(TestEnum.D, new ConstantDoubleVertex(1.75));

        LinkedHashMap<TestEnum, Double> proportions = testSample(selectableValues, random);
        LinkedHashMap<TestEnum, DoubleVertex> normalisedSelectableValues = normaliseSelectableValues(selectableValues, 4.0);
        assertProportionsWithinExpectedRanges(normalisedSelectableValues, proportions);
    }

    private LinkedHashMap<TestEnum, Double> testSample(LinkedHashMap<TestEnum, DoubleVertex> selectableValues,
                                                       Random random) {

        SelectVertex<TestEnum> select = new SelectVertex<>(selectableValues, random);

        LinkedHashMap<TestEnum, Integer> sampleFrequencies = new LinkedHashMap<>();
        sampleFrequencies.put(TestEnum.A, 0);
        sampleFrequencies.put(TestEnum.B, 0);
        sampleFrequencies.put(TestEnum.C, 0);
        sampleFrequencies.put(TestEnum.D, 0);

        for (int i = 0; i < N; i++) {
            TestEnum s = select.sample(random);
            sampleFrequencies.put(s, sampleFrequencies.get(s) + 1);
        }

        return calculateProportions(sampleFrequencies, N);
    }

    private LinkedHashMap<TestEnum, Double> calculateProportions(LinkedHashMap<TestEnum, Integer> sampleFrequencies, int n) {
        LinkedHashMap<TestEnum, Double> proportions = new LinkedHashMap<>();
        for (Map.Entry<TestEnum, Integer> entry : sampleFrequencies.entrySet()) {
            double proportion = (double) entry.getValue() / n;
            proportions.put(entry.getKey(), proportion);
        }

        return proportions;
    }

    private void assertProportionsWithinExpectedRanges(LinkedHashMap<TestEnum, DoubleVertex> selectableValues,
                                                       HashMap<TestEnum, Double> proportions) {

        for (Map.Entry<TestEnum, Double> entry : proportions.entrySet()) {
            log.info(entry.getKey() + ": " + entry.getValue());
            double p = entry.getValue();
            double expected = selectableValues.get(entry.getKey()).getValue();
            assertEquals(p, expected, epsilon);
        }
    }

    private LinkedHashMap<TestEnum, DoubleVertex> normaliseSelectableValues(LinkedHashMap<TestEnum, DoubleVertex> selectableValues,
                                                                            double sum) {
        LinkedHashMap<TestEnum, DoubleVertex> normalised = new LinkedHashMap<>();
        for (Map.Entry<TestEnum, DoubleVertex> entry : selectableValues.entrySet()) {
            double normalizedProbability = entry.getValue().getValue() / sum;
            normalised.put(entry.getKey(), new ConstantDoubleVertex(normalizedProbability));
        }
        return normalised;
    }

    private enum TestEnum {
        A, B, C, D
    }
}