/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package quasylab.sibilla.core.simulator;

import java.io.Serializable;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.commons.math3.random.RandomGenerator;

import quasylab.sibilla.core.simulator.util.WeightedElement;
import quasylab.sibilla.core.simulator.util.WeightedStructure;

/**
 * @author loreti
 *
 */
public class SimulationTask<S> implements Supplier<Trajectory<S>>, Serializable {

	private static final long serialVersionUID = -504798938865475892L;
	private double deadline;
	private double time;
	private Predicate<? super S> transientPredicate;
	private Predicate<? super S> reachPredicate;	
	private boolean reach;
	private RandomGenerator random;
	private Model<S> model;
	private S currentState;
	private SimulationStatus status;
	private Trajectory<S> trajectory;
	private long startTime = 0, elapsedTime = 0;
	
	@SuppressWarnings("unchecked")
	public SimulationTask( RandomGenerator random , Model<S> model , double deadline ) {
		this(random,model,deadline,(Predicate<? super S> & Serializable) s -> false);
	}
	
	@SuppressWarnings("unchecked")
	public SimulationTask( RandomGenerator random , Model<S> model , double deadline , Predicate<? super S> reachPredicate ) {
		this(random,model,deadline,reachPredicate,(Predicate<? super S> & Serializable) s -> true);
	}

	public SimulationTask( RandomGenerator random , Model<S> model , double deadline , Predicate<? super S> reachPredicate, Predicate<? super S> transientPredicate) {
		this.random = random;
		this.model = model;
		this.deadline = deadline;
		this.transientPredicate = transientPredicate;
		this.reachPredicate = reachPredicate;
		this.currentState = model.initialState();
		this.status = SimulationStatus.INIT;
	}

	public void reset(){
		time = 0;
		reach = false;
		currentState = model.initialState();
		status = SimulationStatus.INIT;
		startTime = 0;
		elapsedTime = 0;
	}
	

	@Override
	public Trajectory<S> get() {
		running();
		this.trajectory = new Trajectory<>();
		this.trajectory.add(time, currentState);
		while ((time<deadline)&&(!isCancelled())) {
			if (reachPredicate.test(currentState)) {
				completed(true);
				return this.getTrajectory();
			}
			if (transientPredicate.test(currentState)) {
				step();
			} else {
				completed(false);
				return this.getTrajectory();
			}
		}
		completed(true);
		return this.getTrajectory();
	}

/*
	public void run( ) {
		running();
		this.trajectory = new Trajectory<>();
		this.trajectory.add(time, currentState);
		while ((time<deadline)&&(!isCancelled())) {
			if (reachPredicate.test(currentState)) {
				completed(true);
				return;
			}
			if (transientPredicate.test(currentState)) {
				step();
			} else {
				completed(false);
				return;
			}
		}
		completed(true);
	}
*/
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
		WeightedStructure<StepFunction<S>> agents = 
				this.model.getActivities( random , currentState );
		double totalRate = agents.getTotalWeight();
		if (totalRate == 0.0) {
			cancel();
			return;
		}
		double dt = (1.0 / totalRate) * Math.log(1 / (random.nextDouble()));
		double select = random.nextDouble() * totalRate;
		WeightedElement<StepFunction<S>> wa = agents.select(select);
		if (wa == null) {
			cancel();
			return;
		}
		currentState = wa.getElement().step(random,time,dt);
		time += dt;
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

	public boolean reach() {
		return this.reach;
	}
	
	public Trajectory<S> getTrajectory() {
		return trajectory;		
	}

	public long getElapsedTime(){
		return elapsedTime;
	}

}
