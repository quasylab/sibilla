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

package it.unicam.quasylab.sibilla.core.models.lgio;

import org.apache.commons.math3.random.RandomGenerator;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * This class represents agent attributes.
 */
public class AgentAttributes {

    private final double[] values;

    /**
     * Creates a new instance with the given values.
     *
     * @param values agent attributes.
     */
    public AgentAttributes(double ... values) {
        this.values = values;
    }

    /**
     * Returns the value of attribute with the gi ven index.
     *
     * @param idx attribute index.
     * @return the value of attribute with the gi ven index.
     * @throws IndexOutOfBoundsException when <code>(idx<0)||(idx >= size())</code>
     */
    public double get(int idx) {
        return values[idx];
    }

    /**
     * Returns the number of attributes.
     *
     * @return the number of attributes.
     */
    public int size() {
        return values.length;
    }

    @Override
    public String toString() {
        return Arrays.toString(values);
    }

    public double[] getValues() {
        return Arrays.copyOf(this.values, this.values.length);
    }
}

