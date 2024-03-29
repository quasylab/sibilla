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

package it.unicam.quasylab.sibilla.core.tools;

import org.apache.commons.math3.random.RandomGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * This class represents a probability matrix.
 *
 * @param <S> type of elements indexing rows and columns of the matrix.
 */
public class ProbabilityMatrix<S> {

    private final Map<S, ProbabilityVector<S>> rows;
    private final Function<S, ProbabilityVector<S>> rowsSupplier;


    /**
     * Creates a probability matrix whose elements are generated by the given supplier.
     *
     * @param rowsSupplier supplier used to generate rows of the matrix.
     */
    public ProbabilityMatrix(Function<S, ProbabilityVector<S>> rowsSupplier) {
        this.rowsSupplier = rowsSupplier;
        this.rows = new HashMap<>();
    }

    /**
     * Returns the probability that state <code>from</code> jumps in one step in
     * state <code>to</code>.
     *
     * @param from starting state.
     * @param to reached state.
     * @return the probability that state <code>from</code> jumps in one step in
     * state <code>to</code>.
     */
    public double get(S from, S to) {
        return getRowOf(from).getProbability(to);
     }

    /**
     * Returns the probability vector associated with the given state.
     * @param s a state.
     * @return the probability vector associated with the given state.
     */
    public ProbabilityVector<S> getRowOf(S s) {
        if (this.rows.containsKey(s)) {
            return this.rows.get(s);
        } else {
            ProbabilityVector<S> row = rowsSupplier.apply(s);
            this.rows.put(s, row);
            return row;
        }
    }


    /**
     * Computes the probability distribution obtained after one step from the given vector.
     * @param vector curent probability distribution.
     * @return the probability distribution obtained after one step from the given vector.
     */
    public ProbabilityVector<S> multiply(ProbabilityVector<S> vector) {
        ProbabilityVector<S> result = new ProbabilityVector<>();
        vector.iterate((s,p) -> result.add(getRowOf(s).scale(p)));
        return result;
    }

    public S sample(RandomGenerator randomGenerator, S s) {
        return getRowOf(s).sample(randomGenerator, s);
    }

    /**
     * Iterates the given consumer on all the rows of this matrix.
     *
     * @param consumer the consumer used to evaluate each matrix row.
     */
    public void iterate(BiConsumer<S, ProbabilityVector<S>> consumer) {
        this.rows.entrySet().forEach(e -> consumer.accept(e.getKey(), e.getValue()));
    }
}
