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
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package quasylab.sibilla.core.simulator;

import org.apache.commons.math3.random.RandomGenerator;
import quasylab.sibilla.core.models.StepFunction;
import quasylab.sibilla.core.models.TimeStep;
import quasylab.sibilla.core.past.State;
import quasylab.sibilla.core.simulator.util.WeightedElement;
import quasylab.sibilla.core.simulator.util.WeightedStructure;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * @author loreti
 *
 */
public class SimulationTask<S extends State> implements Supplier<Trajectory<S>>, Serializable {

	private static final long serialVersionUID = -504798938865475892L;

	private double time;
	private RandomGenerator random;
	private SimulationUnit<S> unit;
	private S currentState;
	private SimulationStatus status;
	private Trajectory<S> trajectory;
	private long startTime = 0, elapsedTime = 0;

	private final int index;

	public SimulationTask(RandomGenerator random , SimulationUnit<S> unit) {
		this(-1,random,unit);
	}

	public SimulationTask(int index, RandomGenerator random , SimulationUnit<S> unit) {
		this.random = random;
		this.unit = unit;
		this.status = SimulationStatus.INIT;
		this.index = index;
	}

	public void reset(){
		time = 0;
		status = SimulationStatus.INIT;
		startTime = 0;
		elapsedTime = 0;
	}

	public int getIndex() {
		return index;
	}

	@Override
	public Trajectory<S> get() {
		long startTime = System.nanoTime();
		running();
		this.currentState = this.unit.getState();
		this.trajectory = new Trajectory<>();
		this.trajectory.add(time, currentState);
		while (!unit.getStoppingPredicate().test(this.time, currentState)&&(!isCancelled())) {
			step();
		}
		this.trajectory.setSuccesfull(this.unit.getReachPredicate().check(currentState));
		completed(true);
		this.trajectory.setGenerationTime(System.nanoTime()-startTime);
		return this.getTrajectory();
	}
	
	private synchronized void running() {
		startTime = System.nanoTime();
		if (!isCancelled()) {
			this.status = SimulationStatus.RUNNING;
		}
		
	}

	private synchronized void completed(boolean b) {
		if (this.status != SimulationStatus.CANCELLED) {
			this.status = SimulationStatus.COMPLETED;
		}
		elapsedTime = System.nanoTime() - startTime;
	}

	private void step() {
		TimeStep<S> step  =
				this.unit.getModel().next(random,time,currentState);
		if (step == null) {
			cancel();
			return;
		}
		currentState = step.getValue();
		time += step.getTime();
		trajectory.add(time, currentState);

	}

	public synchronized void cancel() {
		if (!this.isCompleted()) {
			this.status = SimulationStatus.CANCELLED; 			
		}
	}

	public synchronized boolean isCompleted() {
		return this.status == SimulationStatus.COMPLETED;
	}

	public synchronized boolean isRunning() {
		return this.status == SimulationStatus.RUNNING;
	}

	public synchronized boolean isCancelled() {
		return (this.status==SimulationStatus.CANCELLED);
	}

	public Trajectory<S> getTrajectory() {
		return trajectory;		
	}

	public long getElapsedTime(){
		return elapsedTime;
	}

	public SimulationUnit<S> getUnit() {
		return unit;
	}
}
