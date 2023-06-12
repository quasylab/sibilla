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

public class GlobalFractionOfFormula<T, S extends IndexedState<T>> implements GlobalFormula<T,S> {

    private final LocalFormula<T> argument;
    private final DoublePredicate predicate;

    public GlobalFractionOfFormula(LocalFormula<T> argument, DoublePredicate predicate) {
        this.argument = argument;
        this.predicate = predicate;
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
    public GlobalFormula<T, S> next(S state) {
        return new GlobalArrayOfLocalFormula<>(
                IntStream.range(0, state.numberOfAgents()).mapToObj(i -> this.argument.next(state.get(i))).collect(Collectors.toCollection(ArrayList::new)),
                this.predicate
        );
    }

    @Override
    public double getTimeHorizon() {
        return argument.getTimeHorizon();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GlobalFractionOfFormula<?, ?> that = (GlobalFractionOfFormula<?, ?>) o;
        return Objects.equals(argument, that.argument) && Objects.equals(predicate, that.predicate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(argument, predicate);
    }
}
