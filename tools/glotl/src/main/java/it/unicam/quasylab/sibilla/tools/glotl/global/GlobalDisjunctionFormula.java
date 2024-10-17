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

import java.util.Objects;

public class GlobalDisjunctionFormula<T, S extends IndexedState<T>> implements GlobalFormula<T, S> {

    private final GlobalFormula<T, S> firstArgument;
    private final GlobalFormula<T, S> secondArgument;

    public GlobalDisjunctionFormula(GlobalFormula<T, S> firstArgument, GlobalFormula<T, S> secondArgument) {
        this.firstArgument = firstArgument;
        this.secondArgument = secondArgument;
    }


    @Override
    public boolean isAccepting() {
        return firstArgument.isAccepting()&&secondArgument.isAccepting();
    }

    @Override
    public boolean isRejecting() {
        return firstArgument.isRejecting()&& secondArgument.isRejecting();
    }

    @Override
    public GlobalFormula<T,S> next(S state) {
        GlobalFormula<T,S> firstNext = firstArgument.next(state);
        if (firstNext.isAccepting()) {
            return firstNext;
        }
        GlobalFormula<T,S> secondNext = secondArgument.next(state);
        if (secondNext.isAccepting()) {
            return secondNext;
        }
        return GlobalFormula.disjunction(firstNext, secondNext);
    }

    @Override
    public double getTimeHorizon() {
        return Math.max(firstArgument.getTimeHorizon(), secondArgument.getTimeHorizon());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GlobalDisjunctionFormula<?, ?> that = (GlobalDisjunctionFormula<?, ?>) o;
        return Objects.equals(firstArgument, that.firstArgument) && Objects.equals(secondArgument, that.secondArgument);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstArgument, secondArgument);
    }
}
