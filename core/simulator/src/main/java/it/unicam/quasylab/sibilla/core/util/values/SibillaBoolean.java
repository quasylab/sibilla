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
import java.util.function.Supplier;

/**
 * Instances of this class are used to represent boolean values.
 */
public class SibillaBoolean implements SibillaValue {

    /**
     * Constant value representing true.
     */
    public static SibillaBoolean TRUE = new SibillaBoolean(true);

    /**
     * Constant value representing false.
     */
    public static SibillaBoolean FALSE = new SibillaBoolean(false);

    private final boolean value;


    /**
     * Creates an instance with the given value.
     *
     * @param value the boolean value of this instance.
     */
    private SibillaBoolean(boolean value) {
        this.value = value;
    }

    /**
     * Returns the value associated with the given boolean.
     *
     * @param b a boolean value
     * @return the value associated with the given boolean
     */
    public static SibillaValue of(boolean b) {
        if (b) return TRUE;
        return FALSE;
    }

    @Override
    public double doubleOf() {
        return (value?1.0:0.0);
    }

    @Override
    public boolean booleanOf() {
        return value;
    }

    @Override
    public int intOf() {
        return (value?1:0);
    }


    /**
     * Returns the conjunction of this value with the other.
     *
     * @param other another sibilla boolean value.
     * @return the conjunction of this value with the other.
     */
    public SibillaBoolean and(SibillaBoolean other) {
        if ((value)&&other.value) return TRUE;
        return FALSE;
    }

    /**
     * Returns the disjunction of this value with the other.
     *
     * @param other another sibilla boolean value.
     * @return the disjunction of this value with the other.
     */
    public SibillaBoolean or(SibillaBoolean other) {
        if (value|| other.value) return TRUE;
        return FALSE;
    }

    /**
     * Returns the negation of this value.
     *
     * @return the negation of this value.
     */
    public SibillaBoolean not() {
        if (value) return FALSE;
        return TRUE;
    }

    public SibillaValue imply(SibillaBoolean other) {
        if (!value) return TRUE;
        if (other.value) return TRUE;
        return FALSE;
    }

    /**
     * Returns <code>thenValue</code> if this is true, <code>elseValue</code> otherwise.
     *
     * @param thenValue the value returned if this value is true.
     * @param elseValue the value returned if this value is false.
     * @return <code>thenValue</code> if this is true, <code>elseValue</code> otherwise.
     */
    public SibillaValue ifThenElse(SibillaValue thenValue, SibillaValue elseValue) {
        if (value) {
            return thenValue;
        } else {
            return elseValue;
        }
    }

    /**
     * Returns <code>thenSupplier.get()</code> if this is true, <code>elseSupplier.get()</code> otherwise.
     *
     * @param thenSupplier the supplier used if this is true
     * @param elseSupplier the supplier used if this is false
     * @return <code>thenSupplier.get()</code> if this is true, <code>elseSupplier.get()</code> otherwise.
     */
    public SibillaValue ifThenElse(Supplier<SibillaValue> thenSupplier, Supplier<SibillaValue> elseSupplier) {
        if (value) {
            return thenSupplier.get();
        } else {
            return elseSupplier.get();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SibillaBoolean that = (SibillaBoolean) o;
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
