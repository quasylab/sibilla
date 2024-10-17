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
import java.util.function.Predicate;

public class LocalAtomicFormula<T> implements LocalFormula<T> {

    private final Predicate<T> argument;

    public LocalAtomicFormula(Predicate<T> argument) {
        this.argument = argument;
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
    public LocalFormula<T> next(T state) {
        if (argument.test(state)) {
            return new LocalTrueFormula<>();
        } else {
            return new LocalFalseFormula<>();
        }
    }

    @Override
    public double getTimeHorizon() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalAtomicFormula<?> that = (LocalAtomicFormula<?>) o;
        return Objects.equals(argument, that.argument);
    }

    @Override
    public int hashCode() {
        return Objects.hash(argument);
    }
}
