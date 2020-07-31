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
import quasylab.sibilla.core.past.State;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

/**
 * A <code>SimulationManager</code> has the responsibility to coordinate
 * simulation activities.
 *
 * @author Matteo Belenchia, Michele Loreti
 */
public abstract class QueuedSimulationManager<S extends State> extends AbstractSimulationManager<S> {

    private final BlockingQueue<SimulationTask<S>> pendingTasks = new LinkedBlockingQueue<>();
    private int runningTasks = 0;
    private boolean running = true;

    public QueuedSimulationManager(RandomGenerator random, SimulationMonitor monitor, Consumer<Trajectory<S>> trajectoryConsumer) {
        super(random, monitor, trajectoryConsumer);
    }

    /**
     * Starts the handling of the tasks to be executed to complete the simulation
     */
    protected abstract void startTasksHandling();

    /**
     * Add a simulationTask to the tasks to be executed
     *
     * @param simulationTask tasks to be added
     */
    @Override
    protected synchronized void handleTask(SimulationTask<S> simulationTask) {
        pendingTasks.add(simulationTask);
        notifyAll();
    }

    /**
     * Reschedules all the tasks given in input
     *
     * @param tasks tasks to reschedule
     */
    protected synchronized void rescheduleAll(Collection<? extends SimulationTask<S>> tasks) {
        runningTasks -= tasks.size();
        addAll(tasks);
    }

    /**
     * Adds all the given tasks to the tasks to be executed
     *
     * @param tasks tasks to be added
     */
    private synchronized void addAll(Collection<? extends SimulationTask<S>> tasks) {
        pendingTasks.addAll(tasks);
        notifyAll();
    }

    protected synchronized SimulationTask<S> nextTask() {
        try {
            return nextTask(false);
        } catch (InterruptedException e) {
            return null;
        }
    }

    /**
     * Gets the next task to be executed
     *
     * @param blocking whether the operation is blocking or not
     * @return next task to be executed
     * @throws InterruptedException
     */
    protected synchronized SimulationTask<S> nextTask(boolean blocking) throws InterruptedException {
        while (isRunning() && blocking && pendingTasks.isEmpty()) {
            wait();
        }
        SimulationTask<S> task = pendingTasks.poll();
        runningTasks++;
        return task;
    }

    /**
     * Gets the next n tasks to execute
     *
     * @param n number of tasks to be returned
     * @return list of the requested tasks
     */
    protected synchronized List<SimulationTask<S>> getTask(int n) {
        try {
            return getTask(n, false);
        } catch (InterruptedException e) {
            return new LinkedList<>();
        }
    }

    /**
     * Gets the next n tasks to execute
     *
     * @param n        number of tasks to be returned
     * @param blocking whether the operation is blocking or not
     * @return list of the requested tasks
     * @throws InterruptedException
     */
    protected synchronized List<SimulationTask<S>> getTask(int n, boolean blocking) throws InterruptedException {
        while (isRunning() && blocking && pendingTasks.isEmpty()) {
            wait();
        }
        List<SimulationTask<S>> tasks = new LinkedList<>();
        runningTasks += pendingTasks.drainTo(tasks, n);
        return tasks;
    }

    protected synchronized boolean hasTasks() {
        return !pendingTasks.isEmpty();
    }

    protected int getRunningTasks() {
        return runningTasks;
    }

    @Override
    public synchronized int pendingTasks() {
        return pendingTasks.size();
    }
}
