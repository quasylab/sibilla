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

package it.unicam.quasylab.sibilla.tools.bglotl.formulas;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Represents the conjunction formula that is satisfied by all the paths that
 * do satisfy both the arguments.
 *
 * @param firstArgument a local path formula
 * @param secondArgument a local path formula
 */
public record GLoTLLocalPathConjunctionFormula(GLoTLLocalPathFormula firstArgument, GLoTLLocalPathFormula secondArgument)
        implements GLoTLLocalPathFormula {
    @Override
    public <T> GLoTLLocalPathFormula step(T agent, Function<String, Predicate<T>> predicateSolver) {
        return GLoTLLocalPathFormula.conjunction(
                firstArgument.step(agent, predicateSolver),
                secondArgument.step(agent, predicateSolver)
        );
    }

    @Override
    public boolean isSuccessful() {
        return firstArgument().isSuccessful()&&secondArgument().isSuccessful();
    }

    @Override
    public boolean isUnsuccessful() {
        return firstArgument().isUnsuccessful()||secondArgument().isUnsuccessful();
    }

    @Override
    public GLoTLLocalPathFormula negate() {
        return GLoTLLocalPathFormula.disjunction(firstArgument().negate(),secondArgument().negate());
    }
}
