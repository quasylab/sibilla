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

package it.unicam.quasylab.sibilla.tools.glotl.local;

/**
 * This interface is implemented by the classes modelling local formulas of GLoTL.
 *
 * @param <T> types for local states.
 */
public interface LocalFormula<T> {

    static <T> LocalFormula<T> negation(LocalFormula<T> formula) {
        if (formula.isRejecting()) {
            return new LocalTrueFormula<>();
        }
        if (formula.isRejecting()) {
            return new LocalFalseFormula<>();
        }
        return new LocalNegationFormula<>(formula);
    }

    static <T> LocalFormula<T> conjunction(LocalFormula<T> f, LocalFormula<T> g) {
        if (f.isRejecting()|| g.isRejecting()) {
            return new LocalFalseFormula<>();
        }
        if (f.isAccepting()) {
            return g;
        }
        if (g.isAccepting()) {
            return f;
        }
        return new LocalConjunctionFormula<>(f, g);
    }

    static <T> LocalFormula<T> disjunction(LocalFormula<T> f, LocalFormula<T> g) {
        if (f.isAccepting()||g.isAccepting()) {
            return new LocalTrueFormula<>();
        }
        if (f.isRejecting()) {
            return g;
        }
        if (g.isRejecting()) {
            return f;
        }
        return new LocalDisjunctionFormula<>(f, g);
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
    LocalFormula<T> next(T state);

    double getTimeHorizon();

    static <T> LocalFormula<T> imply(LocalFormula<T> f1, LocalFormula<T> f2) {
        return disjunction(new LocalNegationFormula<>(f1), f2);
    }
}
