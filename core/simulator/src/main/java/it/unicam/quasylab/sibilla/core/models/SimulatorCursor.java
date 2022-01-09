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

import org.apache.commons.math3.random.RandomGenerator;

/**
 * Instances of this class can be used to generate a trajectory starting from a given initial state.
 *
 * @param <S> the type of process states.
 */
public interface SimulatorCursor<S extends State> {

    /**
     * This method is invoked when the simulation run starts.
     */
    void start();

    /**
     * Returns the next simulation step. An {@link IllegalStateException} is thrown
     * if the simulation is not started (see {@link SimulatorCursor#start()}).
     *
     * @return the next simulation step.
     */
    boolean step();

    /**
     * Returns the current state. A null value is returned
     * if the simulation is not started (see {@link SimulatorCursor#start()}).
     *
     * @return the current state.
     */
    S currentState();

    /**
     * Returns current time. A {@link Double#NaN} is returned
     * if the simulation is not started (see {@link SimulatorCursor#start()}).
     *
     * @return current time.
     */
    double time();

    /**
     * Returns true if the cursor reached a terminal states, namely a state from which
     * we cannot perform another step.
     *
     * @return true if the cursor reached a terminal states, namely a state from which
     * we cannot perform another step.
     */
    boolean isTerminated();

    /**
     * Returns true if the simulation associated with this cursor is started.
     *
     * @return true if the simulation associated with this cursor is started.
     */
    boolean isStarted();

    /**
     * This method is used to restart the simulation from the initial configuration.
     */
    void restart(RandomGenerator rg);

    /**
     * This method is used to restart the simulation from the initial configuration.
     */
    void restart();

}
