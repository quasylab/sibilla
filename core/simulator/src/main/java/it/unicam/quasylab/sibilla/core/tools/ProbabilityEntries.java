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

import java.util.function.BiFunction;

/**
 * Instances of this class are used to associate a probability value (that is
 * a double value between 0 and 1) to elements of type <code>S</code>.
 *
 * @param <S> type of measured elements
 */
public class ProbabilityEntries<S> {

    private final S entry;

    private final double prob;

    /**
     * Creates a new element associating the given entry with the given value.
     *
     * @param entry an element
     * @param prob the probability value
     */
    public ProbabilityEntries(S entry, double prob) {
        if (prob < 0 || prob > 1) {
            throw new IllegalArgumentException("probability must be between 0 and 1");
        }
        this.entry = entry;
        this.prob = prob;
    }

    /**
     * Returns the element in the entry.
     *
     * @return the element in the entry.
     */
    public S getElement() {
        return entry;
    }

    /**
     * Returns the probability value.
     *
     * @return the probability value.
     */
    public double getProbability() {
        return prob;
    }

    /**
     * Given a function f:S*T -> R, a value t of type T and a probability value p,
     * returns the probability entry associating
     * f(this.getElement(), t) with this.getProbability()*p.
     *
     *
     * @param f a function from S*T to R
     * @param arg a value of type T
     * @param p a probability value
     *
     * @return the probability entry associating
     * f(this.getElement(), t) with this.getProbability()*p
     *
     * @param <T>
     * @param <R>
     */
    public <T,R> ProbabilityEntries<R> apply(BiFunction<S,T,R> f, T arg, double p) {
        return new ProbabilityEntries<>(f.apply(entry, arg), prob*p);
    }

}
