/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 * Copyright (C) 2020.
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package quasylab.sibilla.core.simulator;

import quasylab.sibilla.core.past.State;

import java.util.concurrent.CompletableFuture;

/**
 * A simulation manager has the responsibility to execute a number of simulation
 * units.
 */
public interface SimulationManager<S extends State> {

    /**
     * Schedules the execution of a given {@link SimulationUnit}.
     *
     * @param unit simulation unit to execute.
     */
    void simulate(SimulationUnit<S> unit);

    /**
     * Returns the number of simulation tasks that are currently executed and are
     * not yet terminated.
     *
     * @return the number of simulation tasks that are currently executed and are
     *         not yet terminated.
     */
    int pendingTasks();

    /**
     * Waits until all the pending tasks are terminated.
     *
     * @throws InterruptedException if current thread is interrupted while its
     *                              waiting for ending of simulation.
     */
    void join() throws InterruptedException;

    void shutdown() throws InterruptedException;

    boolean isRunning();

}
