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
 * Represents the until formula that is satisfied by a local path will satisfy the <code>secondArgument</code>
 * in a number of steps between <code>from</code> and <code>to</code> while <code>firstArgument</code> is
 * satisfied.
 *
 * @param firstArgument a local path formula
 * @param from an integer value
 * @param to an integer value
 * @param secondArgument a local path formula
 */
public record GLoTLLocalPathUntilFormula(GLoTLLocalPathFormula firstArgument, int from, int to, GLoTLLocalPathFormula secondArgument) implements GLoTLLocalPathFormula {
    @Override
    public <T> GLoTLLocalPathFormula step(T agent, Function<String, Predicate<T>> predicateSolver) {
        if (from>0) {
            return new GLoTLLocalPathConjunctionFormula(
                    firstArgument.step(agent, predicateSolver),
                    new GLoTLLocalPathUntilFormula(
                            firstArgument,
                            from - 1,
                            to - 1,
                            secondArgument
                    )
            );
        }
        if (to>0) {
            return new GLoTLLocalPathDisjunctionFormula(
                    secondArgument.step(agent, predicateSolver),
                    new GLoTLLocalPathConjunctionFormula(
                            firstArgument.step(agent, predicateSolver),
                            new GLoTLLocalPathUntilFormula(
                                    firstArgument,
                                    0,
                                    to - 1,
                                    secondArgument
                            )
                    )
            );
        }
        return secondArgument.step(agent, predicateSolver);
    }
}
