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


import java.util.Objects;

public class LocalUntilFormula<T> implements LocalFormula<T> {

    private final LocalFormula<T> firstArgument;

    private final int from;

    private final int to;
    private final LocalFormula<T> secondArgument;

    public LocalUntilFormula(LocalFormula<T> firstArgument, int from, int to, LocalFormula<T> secondArgument) {
        if ((from<0)||(to<0)||(from>to)) {
            throw new IllegalArgumentException();
        }
        this.firstArgument = firstArgument;
        this.from = from;
        this.to = to;
        this.secondArgument = secondArgument;
    }


    @Override
    public boolean isAccepting() {
        return firstArgument.isAccepting()&&secondArgument.isAccepting();
    }

    @Override
    public boolean isRejecting() {
        return firstArgument.isRejecting()&&secondArgument.isRejecting();
    }

    @Override
    public LocalFormula<T> next(T state) {
        if (to==0) {
            return secondArgument.next(state);
        }
        if (from>0) {
            return getNextLeft(state);
        }
        return LocalFormula.disjunction(secondArgument.next(state), getNextLeft(state));
    }

    @Override
    public double getTimeHorizon() {
        return to+Math.max(firstArgument.getTimeHorizon()-1, secondArgument.getTimeHorizon());
    }

    private LocalFormula<T> getNextLeft(T state) {
        return LocalFormula.conjunction(firstArgument.next(state),
                new LocalUntilFormula<>(firstArgument, Math.max(0, from-1), Math.max(0, to-1), secondArgument));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalUntilFormula<?> that = (LocalUntilFormula<?>) o;
        return from == that.from && to == that.to && Objects.equals(firstArgument, that.firstArgument) && Objects.equals(secondArgument, that.secondArgument);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstArgument, from, to, secondArgument);
    }
}
