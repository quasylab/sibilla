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
 * Represents the local path formulas.
 */
public sealed interface GLoTLLocalPathFormula permits
        GLoTLLocalPathTrueFormula,
        GLoTLLocalPathFalseFormula,
        GLoTLLocalPathAtomicFormula,
        GLoTLLocalPathNegationFormula,
        GLoTLLocalPathConjunctionFormula,
        GLoTLLocalPathDisjunctionFormula,
        GLoTLLocalPathImplicationFormula,
        GLoTLLocalPathNextFormula,
        GLoTLLocalPathUntilFormula,
        GLoTLLocalPathEventuallyFormula,
        GLoTLLocalPathGloballyFormula {

    GLoTLLocalPathFormula TRUE = new GLoTLLocalPathTrueFormula();
    GLoTLLocalPathFormula FALSE = new GLoTLLocalPathFalseFormula();

    /**
     * Returns the conjunction of the two given formulas. If one of them are unsuccessful a false formula
     * is returned. If both of them are successful, a true formula is returned.
     *
     * @param firstArgument the first argument of the conjunction
     * @param secondArgument the second argument of the conjunction
     * @return the conjunction of the two given formulas.
     */
    static GLoTLLocalPathFormula conjunction(GLoTLLocalPathFormula firstArgument, GLoTLLocalPathFormula secondArgument) {
        if (firstArgument.isUnsuccessful()|| secondArgument.isUnsuccessful()) {
            return FALSE;
        }
        if (firstArgument.isSuccessful()&&secondArgument.isSuccessful()) {
            return TRUE;
        }
        return new GLoTLLocalPathConjunctionFormula(firstArgument,secondArgument);
    }

    /**
     * Returns the disjunction of the two given formulas. If both of them are unsuccessful a false formula
     * is returned. If one********  of them are successful, a true formula is returned.
     *
     * @param firstArgument the first argument of the conjunction
     * @param secondArgument the second argument of the conjunction
     * @return the conjunction of the two given formulas.
     */
    static GLoTLLocalPathFormula disjunction(GLoTLLocalPathFormula firstArgument, GLoTLLocalPathFormula secondArgument) {
        if (firstArgument.isUnsuccessful()&&secondArgument.isUnsuccessful()) {
            return FALSE;
        }
        if (firstArgument.isSuccessful()||secondArgument.isSuccessful()) {
            return TRUE;
        }
        return new GLoTLLocalPathDisjunctionFormula(firstArgument,secondArgument);
    }

    /**
     * Returns the implication of the two given formulas. If the first formula is unsuccessful a true formula
     * is returned. If one********  of them are successful, a true formula is returned.
     *
     * @param firstArgument the first argument of the conjunction
     * @param secondArgument the second argument of the conjunction
     * @return the conjunction of the two given formulas.
     */
    static GLoTLLocalPathFormula implication(GLoTLLocalPathFormula firstArgument, GLoTLLocalPathFormula secondArgument) {
        if (firstArgument.isUnsuccessful()) {
            return TRUE;
        }
        if (firstArgument.isSuccessful()) {
            return secondArgument;
        }
        return new GLoTLLocalPathDisjunctionFormula(firstArgument,secondArgument);
    }

    <T> GLoTLLocalPathFormula step(T agent, Function<String, Predicate<T>> predicateSolver);

    /**
     * Returns the negation of this formula. This method is used to simplify formulas and to avoid
     * multiple negations. The default implementation returns an instance of
     * {@link GLoTLLocalPathNegationFormula} built by using this formula as argument,.
     *
     * @return the negation of this formula.
     */
    default GLoTLLocalPathFormula negate() {
        return new GLoTLLocalPathNegationFormula(this);
    }

    /**
     * Returns true if this formula is successful, false otherwise. A formula is successful if it is equivalent to true.
     * The default implementation returns false.
     *
     * @return true if this formula is successful, false otherwise.
     */
    default boolean isSuccessful() {
        return false;
    }


    /**
     * Returns true if this formula is unsuccessful, false otherwise. A formula is successful if it is equivalent to false.
     * The default implementation returns false.
     *
     * @return true if this formula is unsuccessful, false otherwise.
     */
    default boolean isUnsuccessful() {
        return false;
    }



}
