package io.improbable.keanu.vertices.dbl.probabilistic;

import io.improbable.keanu.distributions.tensors.continuous.TensorBeta;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.KeanuRandom;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.ConstantDoubleVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.diff.PartialDerivatives;

import java.util.Map;

import static io.improbable.keanu.tensor.TensorShapeValidation.checkHasSingleNonScalarShapeOrAllScalar;
import static io.improbable.keanu.tensor.TensorShapeValidation.checkTensorsMatchNonScalarShapeOrAreScalar;

public class BetaVertex extends ProbabilisticDouble {

    private final DoubleVertex alpha;
    private final DoubleVertex beta;

    /**
     * One alpha or beta or both that match a proposed tensor shape of Beta.
     *
     * If all provided parameters are scalar then the proposed shape determines the shape
     *
     * @param shape the desired shape of the vertex
     * @param alpha the alpha of the Beta with either the same shape as specified for this vertex or a scalar
     * @param beta  the beta of the Beta with either the same shape as specified for this vertex or a scalar
     */
    public BetaVertex(int[] shape, DoubleVertex alpha, DoubleVertex beta) {

        checkTensorsMatchNonScalarShapeOrAreScalar(shape, alpha.getShape(), beta.getShape());

        this.alpha = alpha;
        this.beta = beta;
        setParents(alpha, beta);
        setValue(DoubleTensor.placeHolder(shape));
    }

    /**
     * One to one constructor for mapping some shape of alpha and beta to
     * a matching shaped Beta.
     *
     * @param alpha the alpha of the Beta with either the same shape as specified for this vertex or a scalar
     * @param beta  the beta of the Beta with either the same shape as specified for this vertex or a scalar
     */
    public BetaVertex(DoubleVertex alpha, DoubleVertex beta) {
        this(checkHasSingleNonScalarShapeOrAllScalar(alpha.getShape(), beta.getShape()), alpha, beta);
    }

    public BetaVertex(DoubleVertex alpha, double beta) {
        this(alpha, new ConstantDoubleVertex(beta));
    }

    public BetaVertex(double alpha, DoubleVertex beta) {
        this(new ConstantDoubleVertex(alpha), beta);
    }

    public BetaVertex(double alpha, double beta) {
        this(new ConstantDoubleVertex(alpha), new ConstantDoubleVertex(beta));
    }

    public BetaVertex(int[] shape, DoubleVertex alpha, double beta) {
        this(shape, alpha, new ConstantDoubleVertex(beta));
    }

    public BetaVertex(int[] shape, double alpha, DoubleVertex beta) {
        this(shape, new ConstantDoubleVertex(alpha), beta);
    }

    public BetaVertex(int[] shape, double alpha, double beta) {
        this(shape, new ConstantDoubleVertex(alpha), new ConstantDoubleVertex(beta));
    }

    @Override
    public double logPdf(DoubleTensor value) {

        DoubleTensor alphaValues = alpha.getValue();
        DoubleTensor betaValues = beta.getValue();

        DoubleTensor logPdfs = TensorBeta.logPdf(alphaValues, betaValues, value);

        return logPdfs.sum();
    }

    @Override
    public Map<Long, DoubleTensor> dLogPdf(DoubleTensor value) {
        TensorBeta.Diff dlnP = TensorBeta.dlnPdf(alpha.getValue(), beta.getValue(), value);
        return convertDualNumbersToDiff(dlnP.dPdalpha, dlnP.dPdbeta, dlnP.dPdx);
    }

    private Map<Long, DoubleTensor> convertDualNumbersToDiff(DoubleTensor dPdalpha,
                                                             DoubleTensor dPdbeta,
                                                             DoubleTensor dPdx) {

        PartialDerivatives dPdInputsFromAlpha = alpha.getDualNumber().getPartialDerivatives().multiplyBy(dPdalpha);
        PartialDerivatives dPdInputsFromBeta = beta.getDualNumber().getPartialDerivatives().multiplyBy(dPdbeta);
        PartialDerivatives dPdInputs = dPdInputsFromAlpha.add(dPdInputsFromBeta);

        if (!this.isObserved()) {
            dPdInputs.putWithRespectTo(getId(), dPdx);
        }

        return dPdInputs.asMap();
    }

    @Override
    public DoubleTensor sample(KeanuRandom random) {
        return TensorBeta.sample(
            getShape(),
            alpha.getValue(),
            beta.getValue(),
            DoubleTensor.scalar(0.),
            DoubleTensor.scalar(1.),
            random
        );
    }

}
