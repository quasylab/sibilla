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

package it.unicam.quasylab.sibilla.core.tools.glotl.global;

import it.unicam.quasylab.sibilla.core.models.IndexedState;
import it.unicam.quasylab.sibilla.core.tools.glotl.local.LocalFormula;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.DoublePredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GlobalArrayOfLocalFormula<T,S extends IndexedState<T>> implements GlobalFormula<T,S> {

    private final ArrayList<LocalFormula<T>> elements;
    private final DoublePredicate predicate;

    private final double fractionOfSatisfiedFormulas;
    private final double fractionOfUnSatisfiedFormulas;
    private final boolean isTerminal;

    public GlobalArrayOfLocalFormula(ArrayList<LocalFormula<T>> elements, DoublePredicate predicate) {
        this.elements = elements;
        this.predicate = predicate;
        this.fractionOfSatisfiedFormulas = ((double) elements.stream().filter(LocalFormula::isAccepting).count())/elements.size();
        this.fractionOfUnSatisfiedFormulas = ((double) elements.stream().filter(LocalFormula::isRejecting).count())/elements.size();
        this.isTerminal = elements.stream().allMatch(f -> f.isAccepting()||f.isRejecting());
    }

    @Override
    public boolean isAccepting() {
        return isTerminal&&predicate.test(fractionOfSatisfiedFormulas);
    }

    @Override
    public boolean isRejecting() {
        return isTerminal&&predicate.negate().test(1-fractionOfUnSatisfiedFormulas);
    }

    @Override
    public GlobalFormula<T, S> next(S state) {
        if (isAccepting()) {
            return new GlobalTrueFormula<>();
        }
        if (isRejecting()) {
            return new GlobalFalseFormula<>();
        }
        return new GlobalArrayOfLocalFormula<>(IntStream.range(0, elements.size()).mapToObj(i -> elements.get(i).next(state.get(i))).collect(Collectors.toCollection(ArrayList::new)), predicate);
    }

    @Override
    public double getTimeHorizon() {
        return this.elements.stream().mapToDouble(LocalFormula::getTimeHorizon).max().orElse(0.0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GlobalArrayOfLocalFormula<?, ?> that = (GlobalArrayOfLocalFormula<?, ?>) o;
        return Double.compare(that.fractionOfSatisfiedFormulas, fractionOfSatisfiedFormulas) == 0 && Double.compare(that.fractionOfUnSatisfiedFormulas, fractionOfUnSatisfiedFormulas) == 0 && elements.equals(that.elements) && predicate.equals(that.predicate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elements, predicate, fractionOfSatisfiedFormulas, fractionOfUnSatisfiedFormulas);
    }
}
