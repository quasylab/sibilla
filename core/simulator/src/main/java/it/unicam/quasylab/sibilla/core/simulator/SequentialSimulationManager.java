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
import org.apache.commons.math3.random.RandomGenerator;

/**
 * @author belenchia
 *
 */
public class SequentialSimulationManager<S extends State> extends AbstractSimulationManager<S> {

	public SequentialSimulationManager(RandomGenerator random, SimulationMonitor monitor) {
		super(random, monitor);
	}

    @Override
	protected synchronized void handleTask(SimulationTask<S> simulationTask) {
		notifyMonitorStartIteration(simulationTask.getIndex());
		simulationTask.get();
		notifyMonitorEndIteration(simulationTask.getIndex());
	}

	@Override
	public synchronized int pendingTasks() {
		return 0;
	}

	@Override
	public synchronized void join() { }
}