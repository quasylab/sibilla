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

import it.unicam.quasylab.sibilla.core.simulator.util.WeightedStructure;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Optional;
import java.util.function.Function;

public interface MarkovModel<S extends ImmutableState> extends Model<S> {

    @Override
    default SimulatorCursor<S> createSimulationCursor(RandomGenerator r, Function<RandomGenerator, S> initialStateBuilder) {
        return new SimulatorCursor<>() {

            private RandomGenerator rg = r;
            private S current = null;
            private double now = 0.0;
            private boolean terminated = false;
            private boolean started = false;

            @Override
            public void start() {
                this.current = initialStateBuilder.apply(rg);
                this.now = 0.0;
                this.started = true;
                this.terminated = false;
            }

            @Override
            public boolean step() {
                Optional<TimeStep<S>> optionalTimeStep = next(r, now, current);
                if (optionalTimeStep.isPresent()) {
                    recordTimeStep(optionalTimeStep.get());
                    return true;
                } else {
                    terminated = true;
                    return false;
                }
            }

            @Override
            public S currentState() {
                return current;
            }

            @Override
            public double time() {
                return now;
            }

            @Override
            public boolean isTerminated() {
                return terminated;
            }

            @Override
            public boolean isStarted() {
                return started;
            }

            @Override
            public void restart(RandomGenerator rg) {
                this.rg = rg;
                this.current = null;
                this.terminated = false;
                this.started = false;
            }

            @Override
            public void restart() {
                restart(this.rg);
            }

            private void recordTimeStep(TimeStep<S> step) {
                this.current = step.getValue();
                this.now += step.getTime();
            }
        };
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
