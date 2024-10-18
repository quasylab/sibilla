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

package it.unicam.quasylab.sibilla.core.tools.glotl.local;

import java.util.Objects;

public class LocalEventuallyFormula<T> implements LocalFormula<T> {


    private final int from;

    private final int to;
    private final LocalFormula<T> argument;

    public LocalEventuallyFormula(int from, int to, LocalFormula<T> argument) {
        if ((from<0)||(to<0)||(from>to)) {
            throw new IllegalArgumentException();
        }
        this.argument = argument;
        this.from = from;
        this.to = to;
    }


    @Override
    public boolean isAccepting() {
        return argument.isAccepting();
    }

    @Override
    public boolean isRejecting() {
        return argument.isRejecting();
    }

    @Override
    public LocalFormula<T> next(T state) {
        if (to==0) {
            return argument.next(state);
        }
        LocalFormula<T> nextFormula = new LocalEventuallyFormula<>(Math.max(0, from-1), Math.max(0, to-1), argument);
        if (from>0) {
            return nextFormula;
        }
        return LocalFormula.disjunction(argument.next(state), nextFormula);
    }

    @Override
    public double getTimeHorizon() {
        return to+argument.getTimeHorizon();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalEventuallyFormula<?> that = (LocalEventuallyFormula<?>) o;
        return from == that.from && to == that.to && Objects.equals(argument, that.argument);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, argument);
    }
}
