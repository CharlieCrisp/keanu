package io.improbable.keanu.algorithms.mcmc;

import io.improbable.keanu.algorithms.NetworkSamples;
import io.improbable.keanu.algorithms.graphtraversal.VertexValuePropagation;
import io.improbable.keanu.network.BayesNet;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.diff.LogProbGradient;

import java.util.*;

/**
 * Algorithm 3: "Efficient NUTS".
 * The No-U-Turn Sampler: Adaptively Setting Path Lengths in Hamiltonian Monte Carlo
 * https://arxiv.org/pdf/1111.4246.pdf
 */
public class NUTS {

    private final static double DELTA_MAX = 1000.0;

    private NUTS() {
    }

    public static NetworkSamples getPosteriorSamples(final BayesNet bayesNet,
                                                     final List<DoubleVertex> fromVertices,
                                                     final int sampleCount,
                                                     final double stepSize) {

        return getPosteriorSamples(bayesNet, fromVertices, sampleCount, stepSize, new Random());
    }

    public static NetworkSamples getPosteriorSamples(final BayesNet bayesNet,
                                                     final List<? extends Vertex> sampleFromVertices,
                                                     final int sampleCount,
                                                     final double epsilon,
                                                     final Random random) {

        final List<Vertex<Double>> latentVertices = bayesNet.getContinuousLatentVertices();
        final Map<String, Long> latentSetAndCascadeCache = VertexValuePropagation.exploreSetting(latentVertices);
        final List<Vertex> probabilisticVertices = bayesNet.getVerticesThatContributeToMasterP();

        final Map<String, List<?>> samples = new HashMap<>();
        addSampleFromCache(samples, takeSample(sampleFromVertices));

        Map<String, Double> position = new HashMap<>();
        cachePosition(latentVertices, position);

        Map<String, Double> positionForward = new HashMap<>();
        Map<String, Double> positionBackward = new HashMap<>();

        Map<String, Double> gradient = LogProbGradient.getJointLogProbGradientWrtLatents(
                probabilisticVertices
        );
        Map<String, Double> gradientForward = new HashMap<>();
        Map<String, Double> gradientBackward = new HashMap<>();

        Map<String, Double> momentumForward = new HashMap<>();
        Map<String, Double> momentumBackward = new HashMap<>();

        Map<String, ?> sample = takeSample(sampleFromVertices);

        double logOfMasterPreviously = getLogProb(probabilisticVertices);

        for (int sampleNum = 1; sampleNum < sampleCount; sampleNum++) {

            cache(position, positionForward);
            cache(position, positionBackward);

            cache(gradient, gradientForward);
            cache(gradient, gradientBackward);

            initializeMomentumForEachVertex(latentVertices, momentumForward, random);
            cache(momentumForward, momentumBackward);

            double u = random.nextDouble() * Math.exp(logOfMasterPreviously - 0.5 * dotProduct(momentumForward));

            //tree height
            int j = 0;

            //stop flag
            int s = 1;

            //acceptable leapfrog count
            int n = 1;

            while (s == 1) {

                //build tree direction -1 = backwards OR 1 = forwards
                int v = random.nextBoolean() ? 1 : -1;

                BuiltTree builtTree;
                if (v == -1) {
                    builtTree = BuildTree(
                            latentVertices,
                            latentSetAndCascadeCache,
                            probabilisticVertices,
                            sampleFromVertices,
                            positionBackward,
                            gradientBackward,
                            momentumBackward,
                            u,
                            v,
                            j,
                            epsilon,
                            random
                    );

                    positionBackward = builtTree.positionBackward;
                    momentumBackward = builtTree.momentumBackward;
                    gradientBackward = builtTree.gradientBackward;

                } else {

                    builtTree = BuildTree(
                            latentVertices,
                            latentSetAndCascadeCache,
                            probabilisticVertices,
                            sampleFromVertices,
                            positionForward,
                            gradientForward,
                            momentumForward,
                            u,
                            v,
                            j,
                            epsilon,
                            random
                    );

                    positionForward = builtTree.positionForward;
                    momentumForward = builtTree.momentumForward;
                    gradientForward = builtTree.gradientForward;
                }

                if (builtTree.sPrime == 1) {
                    final double acceptanceProb = (double) builtTree.nPrime / n;
                    if (withProbability(acceptanceProb, random)) {
                        position = builtTree.thetaPrime;
                        gradient = builtTree.gradientAtThetaPrime;
                        logOfMasterPreviously = builtTree.logOfMasterPAtThetaPrime;
                        sample = builtTree.sampleAtThetaPrime;
                    }
                }

                n = n + builtTree.nPrime;

                s = builtTree.sPrime * isNotUTurning(
                        positionForward,
                        positionBackward,
                        momentumForward,
                        momentumBackward
                );

                j++;

            }

            addSampleFromCache(samples, sample);
        }

        return new NetworkSamples(samples, sampleCount);
    }

    private static BuiltTree BuildTree(List<Vertex<Double>> latentVertices,
                                       final Map<String, Long> latentSetAndCascadeCache,
                                       List<Vertex> probabilisticVertices,
                                       final List<? extends Vertex> sampleFromVertices,
                                       Map<String, Double> position,
                                       Map<String, Double> gradient,
                                       Map<String, Double> momentum,
                                       double u,
                                       int v,
                                       int j,
                                       double epsilon,
                                       Random random
    ) {

        if (j == 0) {
            //Base case—take one leapfrog step in the direction v

            LeapFrogged leapfrog = leapfrog(
                    latentVertices,
                    latentSetAndCascadeCache,
                    probabilisticVertices,
                    position,
                    gradient,
                    momentum,
                    epsilon * v
            );

            final double logOfMasterPAfterLeapfrog = getLogProb(probabilisticVertices);

            final double logMpMinusMomentum = logOfMasterPAfterLeapfrog - 0.5 * dotProduct(leapfrog.momentum);
            final int nPrime = u <= Math.exp(logMpMinusMomentum) ? 1 : 0;
            final int sPrime = u < Math.exp(DELTA_MAX + logMpMinusMomentum) ? 1 : 0;

            final Map<String, ?> sampleAtThetaPrime = takeSample(sampleFromVertices);

            return new BuiltTree(
                    leapfrog.position,
                    leapfrog.gradient,
                    leapfrog.momentum,
                    logOfMasterPAfterLeapfrog,
                    leapfrog.position,
                    leapfrog.gradient,
                    leapfrog.momentum,
                    logOfMasterPAfterLeapfrog,
                    leapfrog.position,
                    leapfrog.gradient,
                    logOfMasterPAfterLeapfrog,
                    sampleAtThetaPrime,
                    nPrime,
                    sPrime
            );

        } else {
            //Recursion—implicitly build the left and right subtrees.

            BuiltTree leftTree = BuildTree(
                    latentVertices,
                    latentSetAndCascadeCache,
                    probabilisticVertices,
                    sampleFromVertices,
                    position,
                    gradient,
                    momentum,
                    u,
                    v,
                    j - 1,
                    epsilon,
                    random
            );

            Map<String, Double> positionBackward = leftTree.positionBackward;
            Map<String, Double> gradientBackward = leftTree.gradientBackward;
            Map<String, Double> momentumBackward = leftTree.momentumBackward;
            double logOfMasterPAtPositionBackward = leftTree.logOfMasterPAtPositionBackward;

            Map<String, Double> positionForward = leftTree.positionForward;
            Map<String, Double> gradientForward = leftTree.gradientForward;
            Map<String, Double> momentumForward = leftTree.momentumForward;
            double logOfMasterPAtPositionForward = leftTree.logOfMasterPAtPositionForward;

            Map<String, Double> thetaPrime = leftTree.thetaPrime;
            Map<String, Double> gradientAtThetaPrime = leftTree.gradientAtThetaPrime;
            double logOfMasterPAtThetaPrime = leftTree.logOfMasterPAtThetaPrime;
            Map<String, ?> sampleAtThetaPrime = leftTree.sampleAtThetaPrime;

            int sPrime = leftTree.sPrime;
            int nPrime = leftTree.nPrime;

            //Should continue building right tree if left tree stop flag is 1
            if (sPrime == 1) {

                BuiltTree rightTree;
                if (v == -1) {
                    rightTree = BuildTree(
                            latentVertices,
                            latentSetAndCascadeCache,
                            probabilisticVertices,
                            sampleFromVertices,
                            positionBackward,
                            leftTree.gradientBackward,
                            momentumBackward,
                            u,
                            v,
                            j - 1,
                            epsilon,
                            random
                    );

                    positionBackward = rightTree.positionBackward;
                    gradientBackward = rightTree.gradientBackward;
                    momentumBackward = rightTree.momentumBackward;
                    logOfMasterPAtPositionBackward = rightTree.logOfMasterPAtPositionBackward;

                } else {
                    rightTree = BuildTree(
                            latentVertices,
                            latentSetAndCascadeCache,
                            probabilisticVertices,
                            sampleFromVertices,
                            positionForward,
                            leftTree.gradientForward,
                            momentumForward,
                            u,
                            v,
                            j - 1,
                            epsilon,
                            random
                    );

                    positionForward = rightTree.positionForward;
                    gradientForward = rightTree.gradientForward;
                    momentumForward = rightTree.momentumForward;
                    logOfMasterPAtPositionForward = rightTree.logOfMasterPAtPositionForward;
                }

                double acceptThetaPrimeProbability = (double) rightTree.nPrime / (leftTree.nPrime + rightTree.nPrime);

                if (withProbability(acceptThetaPrimeProbability, random)) {
                    thetaPrime = rightTree.thetaPrime;
                    gradientAtThetaPrime = rightTree.gradientAtThetaPrime;
                    logOfMasterPAtThetaPrime = rightTree.logOfMasterPAtThetaPrime;
                    sampleAtThetaPrime = rightTree.sampleAtThetaPrime;
                }

                sPrime = rightTree.sPrime * isNotUTurning(
                        positionForward,
                        positionBackward,
                        momentumForward,
                        momentumBackward
                );

                nPrime = nPrime + rightTree.nPrime;
            }

            return new BuiltTree(
                    positionBackward,
                    gradientBackward,
                    momentumBackward,
                    logOfMasterPAtPositionBackward,
                    positionForward,
                    gradientForward,
                    momentumForward,
                    logOfMasterPAtPositionForward,
                    thetaPrime,
                    gradientAtThetaPrime,
                    logOfMasterPAtThetaPrime,
                    sampleAtThetaPrime,
                    nPrime,
                    sPrime
            );
        }

    }

    private static double getLogProb(List<Vertex> probabilisticVertices) {
        double sum = 0.0;
        for (Vertex<?> vertex : probabilisticVertices) {
            sum += vertex.logProbAtValue();
        }
        return sum;
    }

    private static boolean withProbability(double probability, Random random) {
        return random.nextDouble() < probability;
    }

    private static int isNotUTurning(Map<String, Double> positionForward,
                                     Map<String, Double> positionBack,
                                     Map<String, Double> momentumForward,
                                     Map<String, Double> momentumBack) {
        double forward = 0.0;
        double backward = 0.0;

        for (Map.Entry<String, Double> forwardEntry : positionForward.entrySet()) {

            final String id = forwardEntry.getKey();
            final double forwardMinusBack = forwardEntry.getValue() - positionBack.get(id);

            forward += forwardMinusBack * momentumForward.get(id);
            backward += forwardMinusBack * momentumBack.get(id);
        }

        boolean notTurning = forward >= 0.0 && backward >= 0.0;

        return notTurning ? 1 : 0;
    }

    private static void cachePosition(List<Vertex<Double>> latentVertices, Map<String, Double> position) {
        for (Vertex<Double> vertex : latentVertices) {
            position.put(vertex.getId(), vertex.getValue());
        }
    }

    private static Map<String, Double> initializeMomentumForEachVertex(List<Vertex<Double>> vertexes,
                                                                       Map<String, Double> momentums,
                                                                       Random random) {
        for (int i = 0; i < vertexes.size(); i++) {
            Vertex currentVertex = vertexes.get(i);
            momentums.put(currentVertex.getId(), random.nextGaussian());
        }
        return momentums;
    }

    private static void cache(Map<String, Double> from, Map<String, Double> to) {
        for (Map.Entry<String, Double> entry : from.entrySet()) {
            to.put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * function Leapfrog(θ, r, eps)
     * Set ˜r ← r + (eps/2)∇θL(θ)
     * Set ˜θ ← θ + r˜
     * Set ˜r ← r˜ + (eps/2)∇θL(˜θ)
     * return ˜θ, r˜
     */
    private static LeapFrogged leapfrog(final List<Vertex<Double>> latentVertices,
                                        final Map<String, Long> latentSetAndCascadeCache,
                                        final List<Vertex> probabilisticVertices,
                                        final Map<String, Double> theta,
                                        final Map<String, Double> gradient,
                                        final Map<String, Double> r,
                                        final double epsilon) {

        final double halfTimeStep = epsilon / 2.0;

        Map<String, Double> rPrime = new HashMap<>();
        Map<String, Double> thetaPrime = new HashMap<>();

        //Set r' ← r + (eps/2) * ∇θL(θ)
        for (Map.Entry<String, Double> rEntry : r.entrySet()) {
            final double updatedMomentum = rEntry.getValue() + halfTimeStep * gradient.get(rEntry.getKey());
            rPrime.put(rEntry.getKey(), updatedMomentum);
        }

        //Set θ' ← θ +eps * r'.
        for (Vertex<Double> latent : latentVertices) {
            final double nextPosition = theta.get(latent.getId()) + halfTimeStep * rPrime.get(latent.getId());
            thetaPrime.put(latent.getId(), nextPosition);
            latent.setValue(nextPosition);
        }

        VertexValuePropagation.cascadeUpdate(latentVertices, latentSetAndCascadeCache);

        //Set r'' ← r' + (eps/2) * ∇θL(θ')
        Map<String, Double> thetaPrimeGradient = LogProbGradient.getJointLogProbGradientWrtLatents(
                probabilisticVertices
        );

        for (Map.Entry<String, Double> rPrimeEntry : rPrime.entrySet()) {
            final double rDoublePrime = rPrimeEntry.getValue() + halfTimeStep * thetaPrimeGradient.get(rPrimeEntry.getKey());
            rPrime.put(rPrimeEntry.getKey(), rDoublePrime);
        }

        return new LeapFrogged(thetaPrime, rPrime, thetaPrimeGradient);
    }

    private static double dotProduct(Map<String, Double> momentums) {
        double dotProduct = 0.0;
        for (Double momentum : momentums.values()) {
            dotProduct += momentum * momentum;
        }
        return dotProduct;
    }

    /**
     * This is meant to be used for tracking a sample while building tree.
     *
     * @param sampleFromVertices
     */
    private static Map<String, ?> takeSample(List<? extends Vertex> sampleFromVertices) {
        Map<String, ?> sample = new HashMap<>();
        for (Vertex vertex : sampleFromVertices) {
            putValue(vertex, sample);
        }
        return sample;
    }

    private static <T> void putValue(Vertex<T> vertex, Map<String, ?> target) {
        ((Map<String, T>) target).put(vertex.getId(), vertex.getValue());
    }

    /**
     * This is used to save of the sample from the uniformly chosen thetaPrime position
     *
     * @param samples
     * @param cachedSample a cached sample from before leapfrog
     */
    private static void addSampleFromCache(Map<String, List<?>> samples, Map<String, ?> cachedSample) {
        for (Map.Entry<String, ?> sampleEntry : cachedSample.entrySet()) {
            addSampleForVertex(sampleEntry.getKey(), sampleEntry.getValue(), samples);
        }
    }

    private static <T> void addSampleForVertex(String id, T value, Map<String, List<?>> samples) {
        List<T> samplesForVertex = (List<T>) samples.computeIfAbsent(id, v -> new ArrayList<T>());
        samplesForVertex.add(value);
    }

    private static class LeapFrogged {
        public final Map<String, Double> position;
        public final Map<String, Double> momentum;
        public final Map<String, Double> gradient;

        public LeapFrogged(Map<String, Double> position,
                           Map<String, Double> momentum,
                           Map<String, Double> gradient) {
            this.position = position;
            this.momentum = momentum;
            this.gradient = gradient;
        }
    }

    private static class BuiltTree {

        public final Map<String, Double> positionBackward;
        public final Map<String, Double> gradientBackward;
        public final Map<String, Double> momentumBackward;
        public final double logOfMasterPAtPositionBackward;
        public final Map<String, Double> positionForward;
        public final Map<String, Double> gradientForward;
        public final Map<String, Double> momentumForward;
        public final double logOfMasterPAtPositionForward;
        public final Map<String, Double> thetaPrime;
        public final Map<String, Double> gradientAtThetaPrime;
        public final double logOfMasterPAtThetaPrime;
        public final Map<String, ?> sampleAtThetaPrime;
        public final int nPrime;
        public final int sPrime;

        public BuiltTree(Map<String, Double> positionBackward,
                         Map<String, Double> gradientBackward,
                         Map<String, Double> momentumBackward,
                         double logOfMasterPAtPositionBackward,
                         Map<String, Double> positionForward,
                         Map<String, Double> gradientForward,
                         Map<String, Double> momentumForward,
                         double logOfMasterPAtPositionForward,
                         Map<String, Double> thetaPrime,
                         Map<String, Double> gradientAtThetaPrime,
                         double logOfMasterPAtThetaPrime,
                         Map<String, ?> sampleAtThetaPrime,
                         int nPrime,
                         int sPrime) {

            this.positionBackward = positionBackward;
            this.gradientBackward = gradientBackward;
            this.momentumBackward = momentumBackward;
            this.logOfMasterPAtPositionBackward = logOfMasterPAtPositionBackward;
            this.positionForward = positionForward;
            this.gradientForward = gradientForward;
            this.momentumForward = momentumForward;
            this.logOfMasterPAtPositionForward = logOfMasterPAtPositionForward;
            this.thetaPrime = thetaPrime;
            this.gradientAtThetaPrime = gradientAtThetaPrime;
            this.logOfMasterPAtThetaPrime = logOfMasterPAtThetaPrime;
            this.sampleAtThetaPrime = sampleAtThetaPrime;
            this.nPrime = nPrime;
            this.sPrime = sPrime;
        }
    }

}

