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
import it.unicam.quasylab.sibilla.core.simulator.Trajectory;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Sample;
import it.unicam.quasylab.sibilla.tools.glotl.global.GlobalFormula;

import java.util.function.Predicate;

public class GLoTLPathChecker<T, S extends IndexedState<T>> implements Predicate<Trajectory<S>> {

    private final GlobalFormula<T, S> formula;

    public GLoTLPathChecker(GlobalFormula<T, S> formula) {
        this.formula = formula;
    }


    @Override
    public boolean test(Trajectory<S> trajectory) {
        GlobalFormula<T, S> current = formula;
        for (Sample<S> sample: trajectory.getData()) {
            current = current.next(sample.getValue());
            if (current.isAccepting()) {
                return true;
            }
            if (current.isRejecting()) {
                return false;
            }
        }
        return current.isAccepting();
    }
}
