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

package it.unicam.quasylab.sibilla.tools.glotl;

import it.unicam.quasylab.sibilla.core.models.IndexedState;
import it.unicam.quasylab.sibilla.core.tools.ProbabilityVector;
import it.unicam.quasylab.sibilla.tools.glotl.global.GlobalFormula;

import java.util.Objects;
import java.util.function.Function;

public class ComposedElement<T,S extends IndexedState<T>> implements ModelCheckingState<T, S> {

    private final S state;
    private final GlobalFormula<T,S> formula;

    public ComposedElement(S state, GlobalFormula<T, S> formula) {
        this.state = state;
        this.formula = formula;
    }

    @Override
    public ProbabilityVector<ModelCheckingState<T, S>> next(Function<S, ProbabilityVector<S>> stepFunction) {
        GlobalFormula<T,S> nextFormula = formula.next(this.state);
        if (nextFormula.isAccepting()) {
            return ProbabilityVector.dirac(new AcceptingState<>());
        }
        if (nextFormula.isRejecting()) {
            return ProbabilityVector.dirac(new RejectingState<>());
        }
        return stepFunction.apply(state).map(s -> new ComposedElement<>(s, nextFormula));
    }

    @Override
    public boolean isAccepting() {
        return false;
    }

    @Override
    public boolean isRejecting() {
        return false;
    }

    @Override
    public boolean isPending() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComposedElement<?, ?> that = (ComposedElement<?, ?>) o;
        return state.equals(that.state) && formula.equals(that.formula);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, formula);
    }

    @Override
    public String toString() {
        return "<" +state + ":"+formula +'>';
    }
}
