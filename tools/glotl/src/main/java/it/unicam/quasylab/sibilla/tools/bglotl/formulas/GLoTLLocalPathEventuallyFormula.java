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
 * Represent the formula that is satisfied by a local path that
 * will satisfy <code>argument</code> in a number of steps between <code>from</code>
 * and <code>to</code>,
 *
 * @param argument a local path formula
 * @param from an integer
 * @param to an integer
 */
public record GLoTLLocalPathEventuallyFormula(GLoTLLocalPathFormula argument, int from, int to) implements GLoTLLocalPathFormula {
    @Override
    public <T> GLoTLLocalPathFormula step(T agent, Function<String, Predicate<T>> predicateSolver) {
        if (from>0) {
            return new GLoTLLocalPathEventuallyFormula(argument, from-1, to-1);
        }
        if (to>0) {
            return new GLoTLLocalPathDisjunctionFormula(
                    argument.step(agent, predicateSolver),
                    new GLoTLLocalPathEventuallyFormula(argument, 0, to-1)
            );
        }
        return argument.step(agent, predicateSolver);
    }

    @Override
    public GLoTLLocalPathFormula negate() {
        return new GLoTLLocalPathGloballyFormula(argument.negate(), from, to);
    }
}
