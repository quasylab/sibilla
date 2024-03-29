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

package it.unicam.quasylab.sibilla.core.models;

import it.unicam.quasylab.sibilla.core.simulator.DefaultSimulationCursor;
import it.unicam.quasylab.sibilla.core.simulator.SimulatorCursor;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Optional;
import java.util.function.Function;

public interface MarkovModel<S extends ImmutableState> extends Model<S> {

    @Override
    default SimulatorCursor<S> createSimulationCursor(RandomGenerator r, Function<RandomGenerator, S> initialStateBuilder) {
        return new DefaultSimulationCursor<>(r, this::next, initialStateBuilder);
    }


    /**
     * Returns the simulator cursor starting its execution from the given initial state.
     *
     * @param r random generator to use in the simulation
     * @param initialState initial state
     * @return the simulator cursor starting its execution from the given initial state.
     */
    default SimulatorCursor<S> createSimulationCursor(RandomGenerator r, S initialState) {
        return createSimulationCursor(r, rg -> initialState);
    }

    /**
     * Samples possible next state when the process is in a given state at a given
     * time. A random generator is passed to sample random values when needed.
     *
     * @param r     random generator used to sample needed random values.
     * @param time  current time.
     * @param state current state.
     * @return process time step.
     */
    Optional<TimeStep<S>> next(RandomGenerator r, double time, S state);


}
