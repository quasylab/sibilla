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

package it.unicam.quasylab.sibilla.tools.glotl.global;

import it.unicam.quasylab.sibilla.core.models.IndexedState;

/**
 * This interface is implemented by the classes modelling global formulas of GLoTL.
 *
 * @param <T> types for local states.
 * @param <S> types for global states.
 */
public interface GlobalFormula<T, S extends IndexedState<T>> {

    static <S extends IndexedState<T>, T> GlobalFormula<T,S> negation(GlobalFormula<T,S> formula) {
        if (formula.isRejecting()) {
            return new GlobalTrueFormula<>();
        }
        if (formula.isAccepting()) {
            return new GlobalFalseFormula<>();
        }
        return new GlobalNegationFormula<>(formula);
    }

    static <T, S extends IndexedState<T>> GlobalFormula<T,S> conjunction(GlobalFormula<T,S> f, GlobalFormula<T,S> g) {
        if (f.isRejecting()|| g.isRejecting()) {
            return new GlobalFalseFormula<>();
        }
        if (f.isAccepting()) { return g; }
        if (g.isAccepting()) { return f; }
        return new GlobalConjunctionFormula<>(f, g);
    }

    static <S extends IndexedState<T>, T> GlobalFormula<T,S> disjunction(GlobalFormula<T,S> f, GlobalFormula<T,S> g) {
        if (f.isAccepting()||g.isAccepting()) {
            return new GlobalTrueFormula<>();
        }
        if (f.isRejecting()) { return g; }
        if (g.isRejecting()) { return f; }
        return new GlobalDisjunctionFormula<>(f, g);
    }

    /**
     * Returns true if this formula is successful. A formula is successful if it is satisfied by any path.
     *
     * @return true if this formula is successful.
     */
    boolean isAccepting();

    /**
     * Returns true if this formula is unsuccessful. A formula is successful if no path can  is satisfied by any path.
     *
     * @return true if this formula is successful.
     */
    boolean isRejecting();


    /**
     * Returns the formula that must be satisfied by any state reachable from <code>state</code> in one step
     * to let <code>state</code> satisfies this formula.
     *
     * @param state a state.
     * @return the formula that must be satisfied by any state reachable from <code>state</code> in one step
     * to let <code>state</code> satisfies this formula.
     */
    GlobalFormula<T, S> next(S state);


    /**
     * Returns the last time unit needed to check the satisfaction of this formula.
     *
     * @return the last time unit needed to check the satisfaction of this formula.
     */
    double getTimeHorizon();

    static <T, S extends IndexedState<T>> GlobalFormula<T,S> imply(GlobalFormula<T,S> f1, GlobalFormula<T,S> f2) {
        return disjunction(new GlobalNegationFormula<>(f1), f2);
    }


}
