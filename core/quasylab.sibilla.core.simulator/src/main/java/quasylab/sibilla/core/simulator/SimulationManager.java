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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.math3.random.RandomGenerator;

/**
 * A <code>SimulationManager</code> has the responsibility to coordinate 
 * simulation activities. These are arranged in <it>sessions</it> ({@link SimulationSessionI}).
 * 
 * @author Matteo Belenchia, Michele Loreti
 *
 */
public abstract class SimulationManager<S> {
	
	/**
	 * Session counter.
	 */
	private int sessionCounter = 0;

    private final BlockingQueue<SimulationTask<S>> pendingTasks = new LinkedBlockingQueue<>();

    private int runningTasks = 0;
    private final Consumer<Trajectory<S>> trajectoryConsumer;
    private RandomGenerator random;
    private boolean running = true;
    private final LinkedList<Long> executionTime = new LinkedList<>();

	/**
	 * Creates a new simulation manager.
	 */
	public SimulationManager(RandomGenerator random, Consumer<Trajectory<S>> consumer) {
		this.random = random;
		this.trajectoryConsumer = consumer;
	}
	
	protected abstract void start();

	public synchronized void simulate(SimulationUnit<S> unit) {
		if (!isRunning()) {
			throw new IllegalStateException();
		}
		add(new SimulationTask<>(random, unit));
	}
    
   
	protected void add(SimulationTask<S> simulationTask) {
		pendingTasks.add(simulationTask);
		notify();
	}

	protected synchronized void reschedule(SimulationTask<S> simulationTask) {
		runningTasks--;
		add(simulationTask);
	}
	
	protected synchronized void rescheduleAll( Collection<? extends SimulationTask<S>> tasks ) {
		runningTasks -= tasks.size();
		addAll( tasks );
	}
	
	protected void addAll(Collection<? extends SimulationTask<S>> tasks) {
		pendingTasks.addAll(tasks);
		notify();
	}


	protected synchronized void handleTrajectory( Trajectory<S> trj ) {
		this.executionTime.add(trj.getGenerationTime());
		trajectoryConsumer.accept(trj);
		runningTasks--;
		//notifyAll();
	}

	
	public int computedTrajectories() {
		return executionTime.size();
	}

	
	public double averageExecutionTime() {
		return executionTime.stream().collect(Collectors.averagingDouble(l -> l.doubleValue()));
	}

	
	protected synchronized SimulationTask<S> nextTask() {
		try {
			return nextTask(false);
		} catch (InterruptedException e) {
			return null;
		}
	}
	
	protected synchronized SimulationTask<S> nextTask(boolean blocking) throws InterruptedException {
		while (isRunning()&&blocking&&pendingTasks.isEmpty()) {
			wait();
		}
		runningTasks++;
		return pendingTasks.poll();			
	}

	protected synchronized List<SimulationTask<S>> getTask(int n) {
		try {
			return getTask(n,false);
		} catch (InterruptedException e) {
			return new LinkedList<>();
		}
	}

	protected synchronized List<SimulationTask<S>> getTask(int n, boolean blocking) throws InterruptedException {
		while (isRunning()&&blocking&&pendingTasks.isEmpty()) {
			wait();
		}
		List<SimulationTask<S>> tasks = new LinkedList<>();
		runningTasks += pendingTasks.drainTo(tasks,n);
		return tasks;
	}

	public synchronized boolean isRunning() {
		return running;
	}
	
	public synchronized void setRunning(boolean flag) {
		this.running = flag;
	}

	public void shutdown() throws InterruptedException {
        setRunning(false);
		join();
	}
	
	public synchronized void join() throws InterruptedException {
		while (this.runningTasks>0) {
			wait();
		}
	}

	protected synchronized void executionTerminated() {
		notifyAll();
	}

	protected Consumer<Trajectory<S>> getConsumer() {
		return trajectoryConsumer;
	}

	protected RandomGenerator getRandom(){
		return random;
	}

	protected boolean hasTasks(){
		return !pendingTasks.isEmpty();
	}
}
