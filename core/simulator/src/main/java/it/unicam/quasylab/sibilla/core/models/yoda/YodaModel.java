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

package it.unicam.quasylab.sibilla.core.models.yoda;

import it.unicam.quasylab.sibilla.core.models.*;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class YodaModel<S extends YodaScene> implements InteractiveModel<YodaSystemState<S>>, DiscreteModel<YodaSystemState<S>> {
    private final YodaSystemState<S> systemState;

    public YodaModel(YodaSystemState<S> systemState) {
        this.systemState = systemState;
    }


    @Override
    public List<Action<YodaSystemState<S>>> actions(RandomGenerator r, double time, YodaSystemState<S> state) {
        return null;
    }

    @Override
    public YodaSystemState<S> sampleNextState(RandomGenerator r, double time, YodaSystemState<S> state) {
        return state.next(r);
    }


    @Override
    public int stateByteArraySize() {
        return 0;
    }

    @Override
    public byte[] byteOf(YodaSystemState<S> state) throws IOException {
        return new byte[0];
    }

    @Override
    public YodaSystemState<S> fromByte(byte[] bytes) throws IOException {
        return null;
    }

    @Override
    public String[] measures() {
        return new String[0];
    }

    @Override
    public double measure(String m, YodaSystemState<S> state) {
        return 0;
    }

    @Override
    public Measure getMeasure(String m) {
        return null;
    }

    @Override
    public Predicate getPredicate(String name) {
        return null;
    }

    @Override
    public String[] predicates() {
        return new String[0];
    }
}
