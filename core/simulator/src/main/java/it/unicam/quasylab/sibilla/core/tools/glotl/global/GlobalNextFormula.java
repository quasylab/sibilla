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

public class GlobalNextFormula<T, S extends IndexedState<T>> implements GlobalFormula<T,S> {

    private final int steps;

    private final GlobalFormula<T, S> argument;

    public GlobalNextFormula(int steps, GlobalFormula<T, S> argument) {
        if (steps < 1) {
            throw new IllegalArgumentException("Steps must be greater than 0");
        }
        this.steps = steps;
        this.argument = argument;
    }

    public GlobalNextFormula(GlobalFormula<T, S> argument) {
        this(1, argument);
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
        if (steps>1) {
            return new GlobalNextFormula<>(steps-1, argument);
        } else {
            return argument;
        }
    }

    @Override
    public double getTimeHorizon() {
        return argument.getTimeHorizon()+steps;
    }
}
