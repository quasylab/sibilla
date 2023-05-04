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

public class ProbabilityEntries<S> {

    private final S entry;

    private final double prob;


    public ProbabilityEntries(S entry, double prob) {
        this.entry = entry;
        this.prob = prob;
    }

    public S getElement() {
        return entry;
    }

    public double getProbability() {
        return prob;
    }

    public <T,R> ProbabilityEntries<R> apply(BiFunction<S,T,R> f, T arg, double p) {
        return new ProbabilityEntries<>(f.apply(entry, arg), prob*p);
    }

}
