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

package it.unicam.quasylab.sibilla.core.simulator;

import it.unicam.quasylab.sibilla.core.models.State;
import it.unicam.quasylab.sibilla.core.models.TimeStep;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Optional;
import java.util.function.Function;

public class DefaultSimulationCursor<S extends State> implements SimulatorCursor<S> {

    private RandomGenerator rg;
    private S current = null;
    private double now = 0.0;
    private boolean terminated = false;
    private boolean started = false;

    private final Function<RandomGenerator, S> initialStateBuilder;

    private final SimulationStepFunction<S> stepFunction;

    public DefaultSimulationCursor(RandomGenerator rg, SimulationStepFunction<S> stepFunction, Function<RandomGenerator, S> initialStateBuilder) {
        this.rg = rg;
        this.initialStateBuilder = initialStateBuilder;
        this.stepFunction = stepFunction;
    }

    @Override
    public void start() {
        this.current = initialStateBuilder.apply(rg);
        this.now = 0.0;
        this.started = true;
        this.terminated = false;
    }

    @Override
    public boolean step() {
        Optional<TimeStep<S>> optionalTimeStep = stepFunction.next(rg, now, current);
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
}

