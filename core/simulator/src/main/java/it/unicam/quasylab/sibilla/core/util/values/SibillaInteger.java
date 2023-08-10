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
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;

/**
 * Instances of this class are used to represent integers.
 */
public class SibillaInteger implements SibillaValue {

    private final int value;

    /**
     * Creates an instance representing the given value.
     *
     * @param value integer value.
     */
    public SibillaInteger(int value) {
        this.value = value;
    }


    @Override
    public double doubleOf() {
        return value;
    }

    @Override
    public boolean booleanOf() {
        return value>0;
    }

    @Override
    public int intOf() {
        return value;
    }

    /**
     * Returns the integer resulting from the application of the given binary operator to this value and the given one.
     * @param op the operator to apply
     * @param other the second argument to pass to the operator
     * @return the integer resulting from the application of the given binary operator to this value and the given one
     */
    public SibillaInteger eval(IntBinaryOperator op, SibillaInteger other) {
        return new SibillaInteger(op.applyAsInt(this.value, other.value));
    }

    /**
     * Returns the integer resulting from the application of the given operator to this value.
     *
     * @param op the operator to apply
     * @return the integer resulting from the application of the given operator to this value
     */
    public SibillaInteger eval(IntUnaryOperator op) {
        return new SibillaInteger(op.applyAsInt(this.value));
    }

    /**
     * Returns the integer value of this object.
     *
     * @return the integer value of this object.
     */
    public int getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SibillaInteger that = (SibillaInteger) o;
        return value == that.value;
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
