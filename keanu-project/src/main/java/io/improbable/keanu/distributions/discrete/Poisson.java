package io.improbable.keanu.distributions.discrete;

import java.util.Random;

import static org.apache.commons.math3.util.CombinatoricsUtils.factorial;

/**
 * Computer Generation of Statistical Distributions
 * by Richard Saucier
 * ARL-TR-2168 March 2000
 * 5.2.8 page 49
 */
public class Poisson {
    private final double mu;
    private final Random random;

    public Poisson(double mu, Random random) {
        this.mu = mu;
        this.random = random;
    }

    public int sample() {
        return poisson(mu, random);
    }

    public static int poisson(double mu, Random random) {
        assert (mu > 0.);

        double b = 1.;
        double stopB = Math.exp(-mu);
        int i;

        for (i = 0; b >= stopB; i++) {
            b *= random.nextDouble();
        }

        return i - 1;
    }

    public static double pmf(double mu, int k) {
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
