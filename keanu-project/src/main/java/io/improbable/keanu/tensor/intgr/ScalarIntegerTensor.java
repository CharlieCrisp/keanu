package io.improbable.keanu.tensor.intgr;

import io.improbable.keanu.tensor.bool.BooleanTensor;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import org.apache.commons.lang3.ArrayUtils;

import java.util.function.Function;

public class ScalarIntegerTensor implements IntegerTensor {

    private Integer value;
    private int[] shape;

    public ScalarIntegerTensor(int value) {
        this.value = value;
        this.shape = SCALAR_SHAPE;
    }

    public ScalarIntegerTensor(int[] shape) {
        this.value = null;
        this.shape = shape;
    }

    @Override
    public int getRank() {
        return shape.length;
    }

    @Override
    public int[] getShape() {
        return shape;
    }

    @Override
    public long getLength() {
        return isShapePlaceholder() ? 0L : 1L;
    }

    @Override
    public boolean isShapePlaceholder() {
        return value == null;
    }

    @Override
    public IntegerTensor duplicate() {
        return new ScalarIntegerTensor(value);
    }

    @Override
    public Integer getValue(int... index) {
        if (index.length == 1 && index[0] == 0) {
            return value;
        } else {
            throw new IndexOutOfBoundsException(ArrayUtils.toString(index) + " out of bounds on scalar");
        }
    }

    @Override
    public void setValue(Integer value, int... index) {
        if (index.length == 1 && index[0] == 0) {
            this.value = value;
        } else {
            throw new IndexOutOfBoundsException(ArrayUtils.toString(index) + " out of bounds on scalar");
        }
    }

    @Override
    public Integer sum() {
        return value;
    }

    @Override
    public DoubleTensor toDouble() {
        return DoubleTensor.scalar(value);
    }

    @Override
    public IntegerTensor toInteger() {
        return this;
    }

    @Override
    public Integer scalar() {
        return value;
    }

    @Override
    public IntegerTensor minus(int that) {
        return duplicate().minusInPlace(that);
    }

    @Override
    public IntegerTensor plus(int that) {
        return duplicate().plusInPlace(that);
    }

    @Override
    public IntegerTensor times(int that) {
        return duplicate().timesInPlace(that);
    }

    @Override
    public IntegerTensor div(int that) {
        return duplicate().divInPlace(that);
    }

    @Override
    public IntegerTensor pow(IntegerTensor exponent) {
        return duplicate().powInPlace(exponent);
    }

    @Override
    public IntegerTensor pow(int exponent) {
        return duplicate().powInPlace(exponent);
    }

    @Override
    public IntegerTensor minus(IntegerTensor that) {
        return duplicate().minusInPlace(that);
    }

    @Override
    public IntegerTensor plus(IntegerTensor that) {
        return duplicate().plusInPlace(that);
    }

    @Override
    public IntegerTensor times(IntegerTensor that) {
        return duplicate().timesInPlace(that);
    }

    @Override
    public IntegerTensor div(IntegerTensor that) {
        return duplicate().divInPlace(that);
    }

    @Override
    public IntegerTensor unaryMinus() {
        return duplicate().unaryMinusInPlace();
    }

    @Override
    public IntegerTensor abs() {
        return duplicate().absInPlace();
    }

    @Override
    public IntegerTensor getGreaterThanMask(IntegerTensor greaterThanThis) {
        if (greaterThanThis.isScalar()) {
            return new ScalarIntegerTensor(value > greaterThanThis.scalar() ? 1 : 0);
        } else {
            return IntegerTensor.create(value, greaterThanThis.getShape())
                .getGreaterThanMask(greaterThanThis);
        }
    }

    @Override
    public IntegerTensor getGreaterThanOrEqualToMask(IntegerTensor greaterThanOrEqualToThis) {
        if (greaterThanOrEqualToThis.isScalar()) {
            return new ScalarIntegerTensor(value >= greaterThanOrEqualToThis.scalar() ? 1 : 0);
        } else {
            return IntegerTensor.create(value, greaterThanOrEqualToThis.getShape())
                .getGreaterThanOrEqualToMask(greaterThanOrEqualToThis);
        }
    }

    @Override
    public IntegerTensor getLessThanMask(IntegerTensor lessThanThis) {
        if (lessThanThis.isScalar()) {
            return new ScalarIntegerTensor(value < lessThanThis.scalar() ? 1 : 0);
        } else {
            return IntegerTensor.create(value, lessThanThis.getShape())
                .getLessThanMask(lessThanThis);
        }
    }

    @Override
    public IntegerTensor getLessThanOrEqualToMask(IntegerTensor lessThanOrEqualsThis) {
        if (lessThanOrEqualsThis.isScalar()) {
            return new ScalarIntegerTensor(value <= lessThanOrEqualsThis.scalar() ? 1 : 0);
        } else {
            return IntegerTensor.create(value, lessThanOrEqualsThis.getShape())
                .getLessThanOrEqualToMask(lessThanOrEqualsThis);
        }
    }

    @Override
    public IntegerTensor setWithMaskInPlace(IntegerTensor withMask, int valueToApply) {
        if (withMask.isScalar()) {
            this.value = withMask.scalar() == 1.0 ? valueToApply : this.value;
        } else {
            return IntegerTensor.create(value, withMask.getShape())
                .setWithMaskInPlace(withMask, valueToApply);
        }
        return this;
    }

    @Override
    public IntegerTensor setWithMask(IntegerTensor mask, int value) {
        return duplicate().setWithMaskInPlace(mask, value);
    }

    @Override
    public IntegerTensor apply(Function<Integer, Integer> function) {
        return duplicate().applyInPlace(function);
    }

    @Override
    public IntegerTensor minusInPlace(int that) {
        value = value - that;
        return this;
    }

    @Override
    public IntegerTensor plusInPlace(int that) {
        value = value + that;
        return this;
    }

    @Override
    public IntegerTensor timesInPlace(int that) {
        value = value * that;
        return this;
    }

    @Override
    public IntegerTensor divInPlace(int that) {
        value = value / that;
        return this;
    }

    @Override
    public IntegerTensor powInPlace(IntegerTensor exponent) {
        if (exponent.isScalar()) {
            powInPlace(exponent.scalar());
        } else {
            return IntegerTensor.create(value, exponent.getShape())
                .powInPlace(exponent);
        }
        return this;
    }

    @Override
    public IntegerTensor powInPlace(int exponent) {
        value = (int) Math.pow(value, exponent);
        return this;
    }

    @Override
    public IntegerTensor minusInPlace(IntegerTensor that) {
        if (that.isScalar()) {
            minusInPlace(that.scalar());
        } else {
            return IntegerTensor.create(value, that.getShape())
                .minusInPlace(that);
        }
        return this;
    }

    @Override
    public IntegerTensor plusInPlace(IntegerTensor that) {
        if (that.isScalar()) {
            plusInPlace(that.scalar());
        } else {
            return IntegerTensor.create(value, that.getShape())
                .plusInPlace(that);
        }
        return this;
    }

    @Override
    public IntegerTensor timesInPlace(IntegerTensor that) {
        if (that.isScalar()) {
            timesInPlace(that.scalar());
        } else {
            return IntegerTensor.create(value, that.getShape())
                .timesInPlace(that);
        }
        return this;
    }

    @Override
    public IntegerTensor divInPlace(IntegerTensor that) {
        if (that.isScalar()) {
            divInPlace(that.scalar());
        } else {
            return IntegerTensor.create(value, that.getShape())
                .divInPlace(that);
        }
        return this;
    }

    @Override
    public IntegerTensor unaryMinusInPlace() {
        value = -value;
        return this;
    }

    @Override
    public IntegerTensor absInPlace() {
        value = Math.abs(value);
        return this;
    }

    @Override
    public IntegerTensor applyInPlace(Function<Integer, Integer> function) {
        value = function.apply(value);
        return this;
    }

    @Override
    public BooleanTensor lessThan(int that) {
        return BooleanTensor.scalar(this.value < that);
    }

    @Override
    public BooleanTensor lessThanOrEqual(int that) {
        return BooleanTensor.scalar(this.value <= that);
    }

    @Override
    public BooleanTensor lessThan(IntegerTensor that) {
        if (that.isScalar()) {
            return lessThan(that.scalar());
        } else {
            return that.greaterThan(value);
        }
    }

    @Override
    public BooleanTensor lessThanOrEqual(IntegerTensor that) {
        if (that.isScalar()) {
            return lessThanOrEqual(that.scalar());
        } else {
            return that.greaterThanOrEqual(value);
        }
    }

    @Override
    public BooleanTensor greaterThan(int value) {
        return BooleanTensor.scalar(this.value > value);
    }

    @Override
    public BooleanTensor greaterThanOrEqual(int value) {
        return BooleanTensor.scalar(this.value >= value);
    }

    @Override
    public BooleanTensor greaterThan(IntegerTensor that) {
        if (that.isScalar()) {
            return greaterThan(that.scalar());
        } else {
            return that.lessThan(value);
        }
    }

    @Override
    public BooleanTensor greaterThanOrEqual(IntegerTensor that) {
        if (that.isScalar()) {
            return greaterThanOrEqual(that.scalar());
        } else {
            return that.lessThanOrEqual(value);
        }
    }

    @Override
    public FlattenedView<Integer> getFlattenedView() {
        return new SimpleIntegerFlattenedView(value);
    }

    private static class SimpleIntegerFlattenedView implements FlattenedView<Integer> {

        private int value;

        public SimpleIntegerFlattenedView(int value) {
            this.value = value;
        }

        @Override
        public long size() {
            return 1;
        }

        @Override
        public Integer get(long index) {
            if (index != 0) {
                throw new IndexOutOfBoundsException();
            }
            return value;
        }

        @Override
        public Integer getOrScalar(long index) {
            return value;
        }

        @Override
        public void set(long index, Integer value) {
            if (index != 0) {
                throw new IndexOutOfBoundsException();
            }
            this.value = value;
        }

    }

    @Override
    public double[] asFlatDoubleArray() {
        return new double[]{value};
    }

    @Override
    public int[] asFlatIntegerArray() {
        return new int[]{value};
    }

    @Override
    public Integer[] asFlatArray() {
        return new Integer[]{value};
    }

}
