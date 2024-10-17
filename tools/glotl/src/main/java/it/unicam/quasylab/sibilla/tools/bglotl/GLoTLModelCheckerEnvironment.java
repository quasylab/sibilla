/*
 *  Sibilla:  a Java framework designed to support analysis of Collective
 *  Adaptive Systems.
 *
 *              Copyright (C) ${YEAR}.
 *
 *  See the NOTICE file distributed with this work for additional information
 *  regarding copyright ownership.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *              http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *    or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package it.unicam.quasylab.sibilla.tools.bglotl;

import it.unicam.quasylab.sibilla.core.models.IndexedState;
import it.unicam.quasylab.sibilla.core.tools.ProbabilityVector;
import it.unicam.quasylab.sibilla.core.util.datastructures.Pair;

import java.util.function.Predicate;

public class GLoTLModelCheckerEnvironment<T, S extends IndexedState<T>> {

    public Predicate<S> getGlobalPredicate(String predicateName) {
        return null;
    }

    public ProbabilityVector<S> step(S state) {
        return null;
    }

    public Predicate<T> getLocalPredicate(String predicateName) {
        return null;
    }

    public ProbabilityVector<Pair<T, S>> step(T agent, S context) {
        return null;
    }
}
