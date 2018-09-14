package io.improbable.keanu.distributions.discrete;

import static org.apache.commons.math3.util.CombinatoricsUtils.factorial;

import org.nd4j.linalg.util.ArrayUtil;

import io.improbable.keanu.distributions.DiscreteDistribution;
import io.improbable.keanu.tensor.Tensor;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.tensor.intgr.IntegerTensor;
import io.improbable.keanu.vertices.dbl.KeanuRandom;

/**
 * Computer Generation of Statistical Distributions
 * by Richard Saucier
 * ARL-TR-2168 March 2000
 * 5.2.8 page 49
 */
public class Poisson implements DiscreteDistribution {

    private final DoubleTensor mu;

    public static DiscreteDistribution withParameters(DoubleTensor mu) {
        return new Poisson(mu);
    }

    private Poisson(DoubleTensor mu) {
        this.mu = mu;
    }

    @Override
    public IntegerTensor sample(int[] shape, KeanuRandom random) {
        Tensor.FlattenedView<Double> muWrapped = mu.getFlattenedView();

        int length = ArrayUtil.prod(shape);
        int[] samples = new int[length];
        for (int i = 0; i < length; i++) {
            samples[i] = sample(muWrapped.getOrScalar(i), random);
        }

        return IntegerTensor.create(samples, shape);
    }

    private static int sample(double mu, KeanuRandom random) {
        if (mu <= 0.) {
            throw new IllegalArgumentException("Invalid value for mu: " + mu);
        }

        final double STEP_IN_MU = 500;
        double muLeft = mu;
        int k = 0;
        double p = 1.0;

        /*
         * Algorithm courtesy of Wikipedia:
         * https://en.wikipedia.org/wiki/Poisson_distribution#Generating_Poisson-distributed_random_variables
         *
         * Designed to introduce mu gradually to avoid numerical stability issues
         */
        do {
            k++;
            double u = random.nextDoubleNonZero();
            p *= u;

            while (p < 1.0 && muLeft > 0.0) {
                if (muLeft > STEP_IN_MU) {
                    p *= Math.exp(STEP_IN_MU);
                    muLeft -= STEP_IN_MU;
                } else {
                    p *= Math.exp(muLeft);
                    muLeft = 0.0;
                }
            }
        } while (p > 1.0);

        return k - 1;
    }

    @Override
    public DoubleTensor logProb(IntegerTensor k) {
        Tensor.FlattenedView<Double> muFlattenedView = mu.getFlattenedView();
        Tensor.FlattenedView<Integer> kFlattenedView = k.getFlattenedView();

        double[] result = new double[(int) k.getLength()];
        for (int i = 0; i < result.length; i++) {
            result[i] = Math.log(pmf(muFlattenedView.getOrScalar(i), kFlattenedView.getOrScalar(i)));
        }

        return DoubleTensor.create(result, k.getShape());
    }

    private static double pmf(double mu, int k) {
        if (k >= 0 && k < 20) {
            return (Math.pow(mu, k) / factorial(k)) * Math.exp(-mu);
        } else if (k >= 20) {
            double sumOfFactorial = 0;
            for (int i = 1; i <= k; i++) {
                sumOfFactorial += Math.log(i);
            }
            return Math.exp((k * Math.log(mu)) - sumOfFactorial - mu);
        }
        return 0;
    }
}
