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

package it.unicam.quasylab.sibilla.core.tools.glotl;

import it.unicam.quasylab.sibilla.core.tools.ProbabilityVector;
import it.unicam.quasylab.sibilla.core.models.IndexedState;
import it.unicam.quasylab.sibilla.core.tools.glotl.global.GlobalFormula;

import java.util.function.Function;

public class GLoTLDiscreteTimeModelChecker {

    public <T,S extends IndexedState<T>> double computeProbability(Function<S, ProbabilityVector<S>> stepFunction, S state,
                                                                   GlobalFormula<T, S> formula, double eps) {
        if (formula.isAccepting()) {
            return 1.0;
        }
        if (formula.isRejecting()) {
            return 0.0;
        }
        double residual = 1.0;
        double satProb = 0.0;
        double unsatProb = 0.0;
        ChachedFunction<S, ProbabilityVector<S>> cachedFunction = new ChachedFunction<>(stepFunction);
        ProbabilityVector<ModelCheckingState<T,S>> current = ProbabilityVector.dirac(new ComposedElement<>(state, formula));
        do {
            current = current.apply(ms -> ms.next(cachedFunction));
            satProb += current.get(ModelCheckingState::isAccepting);
            unsatProb += current.get(ModelCheckingState::isRejecting);
            current = current.filter(ModelCheckingState::isPending);
            residual = 1 - (satProb+unsatProb);
        } while (residual > eps);
        return satProb;
    }


}
