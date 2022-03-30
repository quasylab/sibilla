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

package it.unicam.quasylab.sibilla.core.models.slam;

import it.unicam.quasylab.sibilla.core.models.SimulatorCursor;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.function.Function;

public class SlamSimulationCursor implements SimulatorCursor<SlamState> {

    private RandomGenerator rg;
    private SlamState currentState = null;
    private final Function<RandomGenerator, SlamState> initialStateSupplier;

    public SlamSimulationCursor(RandomGenerator rg, Function<RandomGenerator, SlamState> initialStateSupplier) {
        this.rg = rg;
        this.initialStateSupplier = initialStateSupplier;
    }

    @Override
    public void start() {
        this.currentState = initialStateSupplier.apply(rg);
    }

    @Override
    public boolean step() {
        Activity activity = currentState.nextScheduledActivity();
        if (activity != null) {
            activity.execute(rg, currentState);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public SlamState currentState() {
        return currentState;
    }

    @Override
    public double time() {
        if (currentState != null) {
            return currentState.now();
        }
        return Double.NaN;
    }

    @Override
    public boolean isTerminated() {
        if (currentState != null) {
            return currentState.isTerminal();
        }
        return false;
    }

    @Override
    public boolean isStarted() {
        return (currentState != null);
    }

    @Override
    public void restart(RandomGenerator rg) {
        this.rg = rg;
        this.currentState = null;
    }

    @Override
    public void restart() {
        restart(this.rg);
    }
}
