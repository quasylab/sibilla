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

package it.unicam.quasylab.sibilla.core.util.values;

import java.util.Objects;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

/**
 * Instances of this class are used to represent doubles.
 */
public class SibillaDouble implements SibillaValue {

    private final double value;

    /**
     * Creates an instance with the given value.
     *
     * @param value the value to use.
     */
    public SibillaDouble(double value) {
        this.value = value;
    }

    @Override
    public double doubleOf() {
        return value;
    }

    @Override
    public boolean booleanOf() {
        return this.value>0;
    }

    @Override
    public int intOf() {
        return (int) value;
    }

    /**
     * Returns the double obtained from the application of the given operator to this value and the other.
     * @param op the operator to apply.
     * @param other the second argument.
     * @return the double obtained from the application of the given operator to this value and the other.
     */
    public SibillaDouble apply(DoubleBinaryOperator op, SibillaDouble other) {
        return new SibillaDouble(op.applyAsDouble(this.value, other.value));
    }

    /**
     * Returns the double obtained from the application of the given operator to this value.
     *
     * @param op the operator to apply.
     * @return the double obtained from the application of the given operator to this value.
     */
    public SibillaDouble apply(DoubleUnaryOperator op) {
        return new SibillaDouble(op.applyAsDouble(this.value));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SibillaDouble that = (SibillaDouble) o;
        return Double.compare(that.value, value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
