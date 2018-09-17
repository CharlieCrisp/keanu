package io.improbable.keanu.plating;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import static io.improbable.keanu.vertices.VertexMatchers.hasNoLabel;
import static io.improbable.keanu.vertices.VertexMatchers.hasParents;

import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import io.improbable.keanu.network.BayesianNetwork;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.ConstantVertex;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.VertexLabel;
import io.improbable.keanu.vertices.VertexLabelException;
import io.improbable.keanu.vertices.VertexMatchers;
import io.improbable.keanu.vertices.bool.BoolVertex;
import io.improbable.keanu.vertices.bool.nonprobabilistic.BoolProxyVertex;
import io.improbable.keanu.vertices.bool.probabilistic.BernoulliVertex;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.ConstantDoubleVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.DoubleProxyVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.ExponentialVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import io.improbable.keanu.vertices.generic.nonprobabilistic.If;
import io.improbable.keanu.vertices.intgr.IntegerVertex;
import io.improbable.keanu.vertices.intgr.probabilistic.PoissonVertex;

public class PlateBuilderTest {

    private static class Bean {
        public int x;

        public Bean(int x) {
            this.x = x;
        }
    }

    private static final List<Bean> ROWS = Arrays.asList(
        new Bean(0),
        new Bean(0),
        new Bean(0)
    );

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void buildPlatesFromCount_Size() {
        int n = 100;
        Plates plates = new PlateBuilder()
            .count(n)
            .withFactory(plate -> {
            })
            .build();
        assertEquals(n, plates.size());
    }

    @Test
    public void buildPlatesFromCount_PlateContents() {
        int n = 100;
        VertexLabel vertexName = new VertexLabel("vertexName");
        Plates plates = new PlateBuilder<>()
            .count(n)
            .withFactory((plate) -> plate.add(new BernoulliVertex(0.5).labeledAs(vertexName)))
            .build();
        plates.asList().forEach(plate -> {
            assertNotNull(plate.get(vertexName));
        });
    }

    @Test
    public void buildPlatesFromData_Size() {
        Plates plates = new PlateBuilder<Bean>()
            .fromIterator(ROWS.iterator())
            .withFactory((plate, bean) -> {
            })
            .build();
        assertEquals(ROWS.size(), plates.size());
    }

    @Test
    public void buildPlatesFromData_Contents() {
        Plates plates = new PlateBuilder<Bean>()
            .fromIterator(ROWS.iterator())
            .withFactory((plate, bean) -> {
                assertEquals(0, bean.x);
            })
            .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void youCannotAddTheSameLabelTwiceIntoOnePlate() {
        new PlateBuilder<Integer>()
            .count(10)
            .withFactory((plate) -> {
                VertexLabel label = new VertexLabel("x");
                DoubleVertex vertex1 = ConstantVertex.of(1.).labeledAs(label);
                DoubleVertex vertex2 = ConstantVertex.of(1.).labeledAs(label);
                plate.add(vertex1);
                plate.add(vertex2);
            })
            .build();

    }

    @Test
    public void youCanCreateASetOfPlatesWithACommonParameterFromACount() {
        GaussianVertex commonTheta = new GaussianVertex(0.5, 0.01);

        VertexLabel label = new VertexLabel("flip");

        Plates plates = new PlateBuilder<Bean>()
            .count(10)
            .withFactory((plate) -> {
                BoolVertex flip = new BernoulliVertex(commonTheta).labeledAs(label);
                flip.observe(false);
                plate.add(flip);
            })
            .build();


        for (Plate plate : plates) {
            Vertex<DoubleTensor> flip = plate.get(label);
            assertThat(flip.getParents(), contains(commonTheta));
        }
    }

    @Test
    public void youCanPutThePlatesIntoABayesNet() {
        GaussianVertex commonTheta = new GaussianVertex(0.5, 0.01);

        VertexLabel label = new VertexLabel("flip");

        Plates plates = new PlateBuilder<Bean>()
            .count(10)
            .withFactory((plate) -> {
                BoolVertex flip = new BernoulliVertex(commonTheta).labeledAs(label);
                flip.observe(false);
                plate.add(flip);
            })
            .build();

        new BayesianNetwork(commonTheta.getConnectedGraph());
    }

    @Test
    public void itThrowsIfYouTryToPutTheSameVertexIntoMultiplePlates() {
        expectedException.expect(PlateException.class);
        expectedException.expectMessage(containsString("has already been added to Plate_"));

        VertexLabel label = new VertexLabel("theta");
        GaussianVertex commonTheta = new GaussianVertex(0.5, 0.01).labeledAs(label);

        new PlateBuilder<Bean>()
            .count(10)
            .withFactory((plate) -> {
                plate.add(commonTheta);
            })
            .build();
    }


    @Test
    public void youCanCreateASetOfPlatesWithACommonParameterFromAnIterator() {
        GaussianVertex commonTheta = new GaussianVertex(0.5, 0.01);

        VertexLabel label = new VertexLabel("flip");

        Plates plates = new PlateBuilder<Bean>()
            .fromIterator(ROWS.iterator())
            .withFactory((plate, bean) -> {
                BoolVertex flip = new BernoulliVertex(commonTheta).labeledAs(label);
                flip.observe(false);
                plate.add(flip);
            })
            .build();


        for (Plate plate : plates) {
            Vertex<DoubleTensor> flip = plate.get(label);
            assertThat(flip.getParents(), contains(commonTheta));
        }
    }

    /**
     * This is a Hidden Markov Model -
     * see for example http://mlg.eng.cam.ac.uk/zoubin/papers/ijprai.pdf
     *
     * ...  -->  X[t-1]  -->  X[t]  --> ...
     *             |           |
     *           Y[t-1]       Y[t]
     */
    @Test
    public void youCanCreateATimeSeriesFromPlatesFromACount() {

        VertexLabel xLabel = new VertexLabel("x");
        VertexLabel xPreviousLabel = PlateBuilder.proxyFor(xLabel);
        VertexLabel yLabel = new VertexLabel("y");

        Vertex<DoubleTensor> initialX = ConstantVertex.of(1.).labeledAs(xLabel);
        List<Integer> ys = ImmutableList.of(0, 1, 2, 1, 3, 2);

        Plates plates = new PlateBuilder<Integer>()
            .withInitialState(initialX)
            .count(10)
            .withFactory((plate) -> {
                DoubleVertex xPrevious = new DoubleProxyVertex(xPreviousLabel);
                DoubleVertex x = new ExponentialVertex(xPrevious).labeledAs(xLabel);
                IntegerVertex y = new PoissonVertex(x).labeledAs(yLabel);
                plate.add(xPrevious);
                plate.add(x);
                plate.add(y);
            })
            .build();


        Vertex<DoubleTensor> previousX = initialX;

        for (Plate plate : plates) {
            Vertex<DoubleTensor> xPreviousProxy = plate.get(xPreviousLabel);
            Vertex<DoubleTensor> x = plate.get(xLabel);
            Vertex<DoubleTensor> y = plate.get(yLabel);
            assertThat(xPreviousProxy.getParents(), contains(previousX));
            assertThat(x.getParents(), contains(xPreviousProxy));
            assertThat(y.getParents(), contains(x));
            previousX = x;
        }
    }

    /**
     * This is a Hidden Markov Model -
     * see for example http://mlg.eng.cam.ac.uk/zoubin/papers/ijprai.pdf
     *
     * ...  -->  X[t-1]  -->  X[t]  --> ...
     *             |           |
     *           Y[t-1]       Y[t]
     */
    @Test
    public void youCanCreateATimeSeriesFromPlatesFromAnIterator() {

        VertexLabel xLabel = new VertexLabel("x");
        VertexLabel xPreviousLabel = PlateBuilder.proxyFor(xLabel);
        VertexLabel yLabel = new VertexLabel("y");

        Vertex<DoubleTensor> initialX = ConstantVertex.of(1.).labeledAs(xLabel);
        List<Integer> ys = ImmutableList.of(0, 1, 2, 1, 3, 2);

        Plates plates = new PlateBuilder<Integer>()
            .withInitialState(initialX)
            .fromIterator(ys.iterator())
            .withFactory((plate, observedY) -> {
                DoubleVertex xPreviousProxy = new DoubleProxyVertex(xPreviousLabel);
                DoubleVertex x = new ExponentialVertex(xPreviousProxy).labeledAs(xLabel);
                IntegerVertex y = new PoissonVertex(x).labeledAs(yLabel);
                y.observe(observedY);
                plate.add(xPreviousProxy);
                plate.add(x);
                plate.add(y);
            })
            .build();


        Vertex<DoubleTensor> previousX = initialX;

        for (Plate plate : plates) {
            Vertex<DoubleTensor> xPreviousProxy = plate.get(xPreviousLabel);
            Vertex<DoubleTensor> x = plate.get(xLabel);
            Vertex<DoubleTensor> y = plate.get(yLabel);
            assertThat(xPreviousProxy.getParents(), contains(previousX));
            assertThat(x.getParents(), contains(xPreviousProxy));
            assertThat(y.getParents(), contains(x));
            previousX = x;
        }
    }

    /**
     * Note that this behaviour is wrapped by the Loop class
     * See LoopTest.java for example usage
     */
    @Test
    public void youCanCreateALoopFromPlatesFromACount() {
        // inputs
        VertexLabel runningTotalLabel = new VertexLabel("runningTotal");
        VertexLabel stillLoopingLabel = new VertexLabel("stillLooping");
        VertexLabel valueInLabel = new VertexLabel("valueIn");

        // intermediate
        VertexLabel oneLabel = new VertexLabel("one");
        VertexLabel conditionLabel = new VertexLabel("condition");

        // outputs
        VertexLabel plusLabel = new VertexLabel("plus");
        VertexLabel loopLabel = new VertexLabel("loop");
        VertexLabel valueOutLabel = new VertexLabel("valueOut");

        // base case
        DoubleVertex initialSum = ConstantVertex.of(0.).labeledAs(plusLabel);
        BoolVertex tru = ConstantVertex.of(true).labeledAs(loopLabel);
        DoubleVertex initialValue = ConstantVertex.of(0.).labeledAs(valueOutLabel);

        int maximumLoopLength = 100;

        Plates plates = new PlateBuilder<Integer>()
            .withInitialState(initialSum, tru, initialValue)
            .withTransitionMapping(ImmutableMap.of(
                runningTotalLabel, plusLabel,
                stillLoopingLabel, loopLabel,
                valueInLabel, valueOutLabel
            ))
            .count(maximumLoopLength)
            .withFactory((plate) -> {
                // inputs
                DoubleVertex runningTotal = new DoubleProxyVertex(runningTotalLabel);
                BoolVertex stillLooping = new BoolProxyVertex(stillLoopingLabel);
                DoubleVertex valueIn = new DoubleProxyVertex(valueInLabel);
                plate.addAll(ImmutableSet.of(runningTotal, stillLooping, valueIn));

                // intermediate
                DoubleVertex one = ConstantVertex.of(1.).labeledAs(oneLabel);
                BoolVertex condition = new BernoulliVertex(0.5).labeledAs(conditionLabel);
                plate.addAll(ImmutableSet.of(one, condition));

                // outputs
                DoubleVertex plus = runningTotal.plus(one).labeledAs(plusLabel);
                BoolVertex loopAgain = stillLooping.and(condition).labeledAs(loopLabel);
                DoubleVertex result = If.isTrue(loopAgain).then(plus).orElse(valueIn).labeledAs(valueOutLabel);
                plate.addAll(ImmutableSet.of(plus, loopAgain, result));
            })
            .build();


        DoubleVertex previousPlus = initialSum;
        BoolVertex previousLoop = tru;
        DoubleVertex previousValueOut = initialValue;

        for (Plate plate : plates) {
            DoubleVertex runningTotal = plate.get(runningTotalLabel);
            BoolVertex stillLooping = plate.get(stillLoopingLabel);
            DoubleVertex valueIn = plate.get(valueInLabel);

            DoubleVertex one = plate.get(oneLabel);
            BoolVertex condition = plate.get(conditionLabel);

            DoubleVertex plus = plate.get(plusLabel);
            BoolVertex loop = plate.get(loopLabel);
            DoubleVertex valueOut = plate.get(valueOutLabel);

            assertThat(runningTotal.getParents(), contains(previousPlus));
            assertThat(stillLooping.getParents(), contains(previousLoop));
            assertThat(valueIn.getParents(), contains(previousValueOut));

            assertThat(one.getParents(), is(empty()));
            assertThat(condition, hasParents(contains(allOf(
                hasNoLabel(),
                instanceOf(ConstantDoubleVertex.class)
            ))));

            assertThat(plus.getParents(), containsInAnyOrder(runningTotal, one));
            assertThat(loop.getParents(), containsInAnyOrder(condition, stillLooping));
            assertThat(valueOut.getParents(), containsInAnyOrder(loop, valueIn, plus));

            previousPlus = plus;
            previousLoop = loop;
            previousValueOut = valueOut;
        }


        DoubleVertex output = plates.asList().get(maximumLoopLength - 1).get(valueOutLabel);

        for (int firstFailure : new int[]{0, 1, 2, 10, 99}) {
            System.out.format("Testing loop that fails after %d steps%n", firstFailure);
            for (Plate plate : plates) {
                BoolVertex condition = plate.get(conditionLabel);
                condition.setAndCascade(true);
            }
            BoolVertex condition = plates.asList().get(firstFailure).get(conditionLabel);
            condition.setAndCascade(false);
            Double expectedOutput = new Double(firstFailure);
            assertThat(output, VertexMatchers.hasValue(expectedOutput));
        }
    }

    @Test
    public void itThrowsIfTheresAProxyVertexThatItDoesntKnowHowToMap() {
        expectedException.expect(VertexLabelException.class);
        expectedException.expectMessage(startsWith("Cannot find transition mapping for "));
        VertexLabel realLabel = new VertexLabel("real");
        VertexLabel fakeLabel = new VertexLabel("fake");
        Plates plates = new PlateBuilder<Integer>()
            .withInitialState()
            .withTransitionMapping(ImmutableMap.of(realLabel, realLabel))
            .count(10)
            .withFactory((plate) -> {
                plate.add(new DoubleProxyVertex(fakeLabel));
            })
            .build();
    }

    @Test
    public void itThrowsIfTheresAProxyVertexButNoBaseCase() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("You must provide a base case for the Transition Vertices - use withInitialState()");
        VertexLabel realLabel = new VertexLabel("real");
        Plates plates = new PlateBuilder<Integer>()
            .withTransitionMapping(ImmutableMap.of(realLabel, realLabel))
            .count(10)
            .withFactory((plate) -> {
                plate.add(new DoubleProxyVertex(realLabel));
            })
            .build();
    }

    @Test
    public void itThrowsIfTheresAnUnknownLabelInTheProxyMapping() {
        expectedException.expect(VertexLabelException.class);
        expectedException.expectMessage("Cannot find VertexLabel fake");
        VertexLabel realLabel = new VertexLabel("real");
        VertexLabel fakeLabel = new VertexLabel("fake");
        Plates plates = new PlateBuilder<Integer>()
            .withInitialState(ConstantVertex.of(1.).labeledAs(realLabel))
            .withTransitionMapping(ImmutableMap.of(realLabel, fakeLabel))
            .count(10)
            .withFactory((plate) -> {
                plate.add(new DoubleProxyVertex(realLabel));
            })
            .build();
    }
}
