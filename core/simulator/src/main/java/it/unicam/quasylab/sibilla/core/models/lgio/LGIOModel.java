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

package it.unicam.quasylab.sibilla.core.models.lgio;

import it.unicam.quasylab.sibilla.core.models.AbstractModel;
import it.unicam.quasylab.sibilla.core.models.DiscreteModel;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class LGIOModel extends AbstractModel<Collective> implements DiscreteModel<Collective> {


    private final Function<Collective, Function<Agent, ActionProbabilityFunction>> probabilityFunction;


    public LGIOModel(Map<String, Measure<? super Collective>> measuresTable, Map<String, Predicate<? super Collective>> predicatesTable, Function<Collective, Function<Agent, ActionProbabilityFunction>> probabilityFunction) {
        super(measuresTable, predicatesTable);
        this.probabilityFunction = probabilityFunction;
    }

    public LGIOModel(Map<String, Measure<? super Collective>> measuresTable, Function<Collective, Function<Agent, ActionProbabilityFunction>> probabilityFunction) {
        super(measuresTable);
        this.probabilityFunction = probabilityFunction;
    }

    @Override
    public Collective sampleNextState(RandomGenerator r, double time, Collective state) {
        return state.step(r, getProbabilityFunction(state));
    }

    private Function<Agent, ActionProbabilityFunction> getProbabilityFunction(Collective collective) {
        return this.probabilityFunction.apply(collective);
    }

    @Override
    public int stateByteArraySize() {
        return 0;
    }

    @Override
    public byte[] byteOf(Collective state) throws IOException {
        return new byte[0];
    }

    @Override
    public Collective fromByte(byte[] bytes) throws IOException {
        return null;
    }
}
