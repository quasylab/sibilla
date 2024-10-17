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

public class GlobalNegationFormula<T, S extends IndexedState<T>> implements GlobalFormula<T, S> {

    private final GlobalFormula<T, S> argument;

    public GlobalNegationFormula(GlobalFormula<T, S> argument) {
        this.argument = argument;
    }

    @Override
    public boolean isAccepting() {
        return argument.isRejecting();
    }

    @Override
    public boolean isRejecting() {
        return argument.isAccepting();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GlobalNegationFormula<?, ?> that = (GlobalNegationFormula<?, ?>) o;
        return Objects.equals(argument, that.argument);
    }

    @Override
    public int hashCode() {
        return Objects.hash(argument);
    }

    @Override
    public GlobalFormula<T,S> next(S state) {
        return GlobalFormula.negation( argument.next(state) );
    }

    @Override
    public double getTimeHorizon() {
        return argument.getTimeHorizon();
    }
}
