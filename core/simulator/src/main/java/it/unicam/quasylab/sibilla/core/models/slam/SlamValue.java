/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 *             Copyright (C) 2020.
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package it.unicam.quasylab.sibilla.core.models.slam;

import java.util.Arrays;
import java.util.function.*;
import java.util.stream.IntStream;

/**
 * Identifies the values handled by SLAM agents.
 */
public interface SlamValue {

    SlamValue NONE = new NoneValue();
    SlamValue FALSE = new BooleanValue(false);
    SlamValue TRUE = new BooleanValue(true);
    IntFunction<SlamValue> INT_VALUE = IntegerValue::new;
    DoubleFunction<SlamValue> REAL_VALUE = RealValue::new;
    BiFunction<SlamType,SlamValue[],SlamValue> LIST_VALUE = ListValue::new;

    static SlamValue booleanValueOf(double v) {
        if (v>0) {
            return TRUE;
        } else {
            return FALSE;
        }
    }

    static SlamValue booleanValueOf(int v) {
        if (v>0) {
            return TRUE;
        } else {
            return FALSE;
        }
    }

    SlamType getType();

    /**
     * Returns the cast of this value to the given {@link SlamType}.
     *
     * @param type type to cast this value.
     * @return the cast of this value to the given {@link SlamType}.
     */
    default SlamValue cast(SlamType type) {
        return NONE;
    }

    /**
     * Returns the sum of this value with the other.
     *
     * @param other another value.
     * @return the sum of this value with the other.
     */
    default SlamValue sum(SlamValue other) {
        return NONE;
    }

    /**
     * Returns the difference of this value with the other.
     *
     * @param other another value.
     * @return the difference of this value with the other.
     */
    default SlamValue sub(SlamValue other) {
        return NONE;
    }

    /**
     * Returns the multiplication of this value with the other.
     *
     * @param other another value.
     * @return the multiplication of this value with the other.
     */
    default SlamValue mul(SlamValue other) {
        return NONE;
    }

    /**
     * Returns the division of this value with the other.
     *
     * @param other another value.
     * @return the division of this value with the other.
     */
    default SlamValue div(SlamValue other) {
        return NONE;
    }


    /**
     * Returns the modulo of this value with the other.
     *
     * @param other another value.
     * @return the modulo of this value with the other.
     */
    default SlamValue mod(SlamValue other) {
        return NONE;
    }

    /**
     * Returns the conjunction of this value with the other.
     *
     * @param other another value.
     * @return the conjunction of this value with the other.
     */
    default SlamValue conjunction(SlamValue other) {
        return NONE;
    }

    /**
     * Returns the disjunction of this value with the other.
     *
     * @param other another value.
     * @return the disjunction of this value with the other.
     */
    default SlamValue disjunction(SlamValue other) {
        return NONE;
    }

    /**
     * Returns the negation of this value.
     *
     * @return the negation of this value with the other.
     */
    default SlamValue negation() {
        return NONE;
    }

    /**
     * If this values is a sequence, returns the head of the sequence.
     *
     * @return the head of the sequence represented by this value.
     */
    default SlamValue head() {
        return NONE;
    }

    /**
     * If this values is a sequence, returns the tail of the sequence.
     *
     * @return the tail of the sequence represented by this value.
     */
    default SlamValue tail() {
        return NONE;
    }

    /**
     * If this values is a sequence, returns the element indexed by the give parameter.
     *
     * @return the element indexed by the give parameter.
     */
    default SlamValue select(SlamValue index) {
        return NONE;
    }

    /**
     * Returns the concatenation of this sequence with the one passed as parameter.
     *
     * @param other a value representing a sequence.
     * @return the concatenation of this sequence with the one passed as parameter.
     */
    default SlamValue concat(SlamValue other) {
        return NONE;
    }

    /**
     * Returns the evaluation of the first parameter if this value is <code>true</code> or the evaluation of
     * the second parameter if this is <code>false</code>.
     *
     * @param thenValue a value supplier
     * @param elseValue a value supplier
     * @return the evaluation of the first parameter if this value is <code>true</code> or the evaluation of
     * the second parameter if this is <code>false</code>.
     */
    default SlamValue ifThenElse(Supplier<SlamValue> thenValue, Supplier<SlamValue> elseValue) {
        return NONE;
    }

    /**
     * Returns the absolute value of this value  (see {@link Math#abs(double)}).
     *
     * @return the absolute value of this value.
     */
    default SlamValue abs() {
        return NONE;
    }

    /**
     * Returns the acos value of this value  (see {@link Math#acos(double)}).
     *
     * @return the acos value of this value.
     */
    default SlamValue acos() {
        return NONE;
    }

    /**
     * Returns the asin value of this value (see {@link Math#sin(double)}).
     *
     * @return the asin value of this value.
     */
    default SlamValue asin() {
        return NONE;
    }

    /**
     * Returns the arc tangent of this value (see {@link Math#atan(double)}.
     *
     * @return the arc tangent of this value.
     */
    default SlamValue atan() {
        return NONE;
    }

    /**
     * Returns the angle theta from the conversion of rectangular
     * coordinates (other, this) to polar coordinates (r, theta)
     * (see {@link Math#atan2(double, double)}.
     *
     * @return the atan2 tangent of this value.
     */
    default SlamValue atan2(SlamValue other) {
        return NONE;
    }

    /**
     * Returns the ceil this value (see {@link Math#ceil(double)}.
     *
     * @return the ceil of this value.
     */
    default SlamValue ceil() {
        return NONE;
    }

    /**
     * Returns the cos of this value (see {@link Math#cos(double)}.
     *
     * @return the cos of this value.
     */
    default SlamValue cos() {
        return NONE;
    }

    /**
     * Returns the cosh of this value (see {@link Math#cosh(double)}.
     *
     * @return the cosh of this value.
     */
    default SlamValue cosh() {
        return NONE;
    }

    /**
     * Returns the exp this value (see {@link Math#exp(double)}.
     *
     * @return the exp of this value.
     */
    default SlamValue exp() {
        return NONE;
    }

    /**
     * Returns the floor of this value (see {@link Math#floor(double)}.
     *
     * @return the floor of this value.
     */
    default SlamValue floor() {
        return NONE;
    }

    /**
     * Returns the log of this value (see {@link Math#log(double)}.
     *
     * @return the log of this value.
     */
    default SlamValue log() {
        return NONE;
    }

    /**
     * Returns the log10 of this value (see {@link Math#log10(double)}.
     *
     * @return the log10 of this value.
     */
    default SlamValue log10() {
        return NONE;
    }

    /**
     * Returns the max between this value and the given one.
     *
     * @param other a value to compare.
     * @return the max between this value and the given one.
     */
    default SlamValue max(SlamValue other) {
        return NONE;
    }

    /**
     * Returns the min between this value and the given one.
     *
     * @param other a value to compare.
     * @return the min between this value and the given one.
     */
    default SlamValue min(SlamValue other) {
        return NONE;
    }

    /**
     * Returns the pow of this value to the given parameter (see {@link Math#pow(double, double)}).
     *
     * @param other the value of the exponent.
     * @return the pow of this value to the given parameter (see {@link Math#pow(double, double)}).
     */
    default SlamValue pow(SlamValue other) {
        return NONE;
    }

    /**
     * Returns the sin of this value (see {@link Math#sin(double)}.
     *
     * @return the sin of this value.
     */
    default SlamValue sin() {
        return NONE;
    }

    /**
     * Returns the sinh of this value (see {@link Math#sinh(double)}.
     *
     * @return the sinh of this value.
     */
    default SlamValue sinh() {
        return NONE;
    }

    /**
     * Returns the tan of this value (see {@link Math#tan(double)}.
     *
     * @return the tan of this value.
     */
    default SlamValue tan() {
        return NONE;
    }

    /**
     * Returns the tanh of this value (see {@link Math#tanh(double)}.
     *
     * @return the tanh of this value.
     */
    default SlamValue tanh() {
        return NONE;
    }

    /**
     * Returns the value representing true if this is a true value, a non negative number, or a non empty sequence.
     *
     * @return the value representing true if this is a true value, a non negative number, or a non empty sequence.
     */
    default SlamValue isTrue() {
        return NONE;
    }

    /**
     * Returns the representation of this value as a real if this exists.
     *
     * @return the representation of this value as a real if this exists.
     */
    default SlamValue realValue() {
        return NONE;
    }

    /**
     * Returns the representation of this value as an integer if this exists.
     *
     * @return the representation of this value as an integer if this exists.
     */
    default SlamValue intValue()  {
        return NONE;
    }

    /**
     * Returns the length of the sequence identified by this value or <code>NONE</code>
     * if this is not a sequence.
     *
     * @return the length of the sequence identified by this value or <code>NONE</code>
     * if this is not a sequence.
     */
    default SlamValue length() {
        return NONE;
    }

    /**
     * Returns the sequence obtained by adding the given value at the end of this sequence. If this is not
     * a sequence, NONE is returned.
     *
     * @param value the value to append.
     * @return the sequence obtained by appending the given value to this sequence. If this is not
     * a sequence, NONE is returned.
     */
    default SlamValue addLast(SlamValue value) {
        return NONE;
    }

    /**
     * Returns the sequence obtained by adding the given value at the beginning of this sequence. If this is not
     * a sequence, NONE is returned.
     *
     * @param value the value to append.
     * @return the sequence obtained by appending the given value to this sequence. If this is not
     * a sequence, NONE is returned.
     */
    default SlamValue addFirst(SlamValue value) {
        return NONE;
    }


    class NoneValue implements SlamValue {

        @Override
        public SlamType getType() {
            return SlamType.NONE_TYPE;
        }

    }

    class BooleanValue implements SlamValue {

        private final boolean value;

        public BooleanValue(boolean value) {
            this.value = value;
        }

        @Override
        public SlamType getType() {
            return SlamType.BOOLEAN_TYPE;
        }

        @Override
        public SlamValue conjunction(SlamValue other) {
            if (other.getType() == SlamType.BOOLEAN_TYPE) {
                return (value?other:this);
            }
            return NONE;
        }

        @Override
        public SlamValue disjunction(SlamValue other) {
            if (other.getType() == SlamType.BOOLEAN_TYPE) {
                return (value?this:other);
            }
            return NONE;
        }

        @Override
        public SlamValue negation() {
            return (value?FALSE:TRUE);
        }

        @Override
        public SlamValue ifThenElse(Supplier<SlamValue> thenValue, Supplier<SlamValue> elseValue) {
            return (value?thenValue.get():elseValue.get());
        }
    }

    class IntegerValue implements SlamValue {

        private final int value;

        @Override
        public SlamValue isTrue() {
            return booleanValueOf(value);
        }

        @Override
        public SlamType getType() {
            return SlamType.INTEGER_TYPE;
        }

        public IntegerValue(int value) {
            this.value = value;
        }

        private IntegerValue doApply(IntUnaryOperator op) {
            return new IntegerValue(op.applyAsInt(this.value));
        }

        private IntegerValue doApply(IntBinaryOperator op, IntegerValue other) {
            return new IntegerValue(op.applyAsInt(this.value, other.value));
        }


        @Override
        public SlamValue sum(SlamValue other) {
            if (other.getType()==SlamType.INTEGER_TYPE) {
                return doApply(Integer::sum, (IntegerValue) other);
            }
            return NONE;
        }

        @Override
        public SlamValue sub(SlamValue other) {
            if (other.getType()==SlamType.INTEGER_TYPE) {
                return doApply((x,y) -> x-y, (IntegerValue) other);
            }
            return NONE;
        }

        @Override
        public SlamValue mul(SlamValue other) {
            if (other.getType()==SlamType.INTEGER_TYPE) {
                return doApply((x,y) -> x*y, (IntegerValue) other);
            }
            return NONE;
        }

        @Override
        public SlamValue div(SlamValue other) {
            if (other.getType()==SlamType.INTEGER_TYPE) {
                IntegerValue otherInteger = (IntegerValue) other;
                if (otherInteger.value == 0) {
                    return NONE;
                }
                return doApply((x,y) -> x/y, otherInteger);
            }
            return NONE;
        }

        @Override
        public SlamValue mod(SlamValue other) {
            if (other.getType()==SlamType.INTEGER_TYPE) {
                return doApply((x,y) -> x%y, (IntegerValue) other);
            }
            return NONE;
        }

        @Override
        public SlamValue abs() {
            return doApply(Math::abs);
        }

        @Override
        public SlamValue max(SlamValue other) {
            if (other.getType()==SlamType.INTEGER_TYPE) {
                return doApply(Math::max, (IntegerValue) other);
            }
            return NONE;
        }

        @Override
        public SlamValue min(SlamValue other) {
            if (other.getType()==SlamType.INTEGER_TYPE) {
                return doApply(Math::min, (IntegerValue) other);
            }
            return NONE;
        }

        @Override
        public SlamValue realValue() {
            return REAL_VALUE.apply(this.value);
        }

        @Override
        public SlamValue intValue() {
            return this;
        }

        @Override
        public int hashCode() {
            return this.value;
        }

        @Override
        public String toString() {
            return ""+value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            IntegerValue that = (IntegerValue) o;
            return value == that.value;
        }
    }

    class RealValue implements SlamValue {

        private final double value;

        public RealValue(double value) {
            this.value = value;
        }

        private RealValue doApply(DoubleUnaryOperator op) {
            return new RealValue(op.applyAsDouble(this.value));
        }

        private RealValue doApply(DoubleBinaryOperator op, RealValue other) {
            return new RealValue(op.applyAsDouble(this.value, other.value));
        }


        @Override
        public SlamType getType() {
            return SlamType.REAL_TYPE;
        }

        @Override
        public SlamValue sum(SlamValue other) {
            if (SlamType.REAL_TYPE==other.getType()) {
                return doApply(Double::sum, ((RealValue) other));
            }
            return NONE;
        }

        @Override
        public SlamValue sub(SlamValue other) {
            if (SlamType.REAL_TYPE==other.getType()) {
                return doApply((x,y) -> x-y, ((RealValue) other));
            }
            return NONE;
        }

        @Override
        public SlamValue mul(SlamValue other) {
            if (SlamType.REAL_TYPE==other.getType()) {
                return doApply((x,y) -> x*y, ((RealValue) other));
            }
            return NONE;
        }

        @Override
        public SlamValue mod(SlamValue other) {
            if (SlamType.REAL_TYPE==other.getType()) {
                return doApply((x,y) -> x%y, ((RealValue) other));
            }
            return NONE;
        }

        @Override
        public SlamValue log() {
            return doApply(Math::log);
        }

        @Override
        public SlamValue log10() {
            return doApply(Math::log10);
        }

        @Override
        public SlamValue max(SlamValue other) {
            if (SlamType.REAL_TYPE==other.getType()) {
                return doApply(Math::max, ((RealValue) other));
            }
            return NONE;
        }

        @Override
        public SlamValue min(SlamValue other) {
            if (SlamType.REAL_TYPE==other.getType()) {
                return doApply(Math::min, ((RealValue) other));
            }
            return NONE;
        }

        @Override
        public SlamValue pow(SlamValue other) {
            if (SlamType.REAL_TYPE==other.getType()) {
                return doApply(Math::pow, ((RealValue) other));
            }
            return NONE;
        }

        @Override
        public SlamValue sin() {
            return doApply(Math::sin);
        }

        @Override
        public SlamValue sinh() {
            return doApply(Math::sinh);
        }

        @Override
        public SlamValue tan() {
            return doApply(Math::tan);
        }

        @Override
        public SlamValue tanh() {
            return doApply(Math::tanh);
        }

        @Override
        public SlamValue isTrue() {
            return booleanValueOf(value);
        }

        @Override
        public SlamValue realValue() {
            return this;
        }

        @Override
        public SlamValue intValue() {
            return INT_VALUE.apply((int) value);
        }
    }

    class ListValue implements SlamValue {

        private final SlamType elementType;
        private final SlamValue[] values;


        public ListValue(SlamType elementType, SlamValue ... values) {
            this.elementType = elementType;
            this.values = values;
        }

        @Override
        public SlamType getType() {
            return null;
        }

        @Override
        public SlamValue head() {
            if (values.length>0) {
                return values[0];
            }
            return NONE;
        }

        @Override
        public SlamValue tail() {
            return new ListValue(this.elementType, Arrays.copyOfRange(this.values,1,this.values.length));
        }

        @Override
        public SlamValue select(SlamValue index) {
            if (index.getType() != SlamType.INTEGER_TYPE) {
                return NONE;
            }
            IntegerValue indexValue = (IntegerValue) index;
            if ((indexValue.value<0)||(indexValue.value>=values.length)) {
                return NONE;
            }
            return this.values[indexValue.value];
        }

        @Override
        public SlamValue concat(SlamValue other) {
            return SlamValue.super.concat(other);
        }

        @Override
        public SlamValue isTrue() {
            return booleanValueOf(values.length);
        }

        @Override
        public SlamValue length() {
            return INT_VALUE.apply(values.length);
        }

        @Override
        public SlamValue addLast(SlamValue value) {
            if (value.getType().equals(this.elementType)) {
                SlamValue[] newValues = IntStream.range(0, this.values.length + 1)
                        .mapToObj(i -> (i<this.values.length?this.values[i]:value))
                        .toArray(SlamValue[]::new);
                return new ListValue(this.elementType, newValues);
            }
            return NONE;
        }

        @Override
        public SlamValue addFirst(SlamValue value) {
            if (value.getType().equals(this.elementType)) {
                SlamValue[] newValues = IntStream.range(0, this.values.length + 1)
                        .mapToObj(i -> (i>0?this.values[i-1]:value))
                        .toArray(SlamValue[]::new);
                return new ListValue(this.elementType, newValues);
            }
            return NONE;
        }
    }
}
