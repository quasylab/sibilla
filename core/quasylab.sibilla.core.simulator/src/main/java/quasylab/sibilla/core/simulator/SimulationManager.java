/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 *  Copyright (C) 2020.
 *
 *  See the NOTICE file distributed with this work for additional information
 *  regarding copyright ownership.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package quasylab.sibilla.core.simulator;

import org.apache.commons.math3.random.RandomGenerator;
import quasylab.sibilla.core.simulator.pm.State;

import javax.swing.event.SwingPropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A <code>SimulationManager</code> has the responsibility to coordinate
 * simulation activities. These are arranged in <it>sessions</it> ({@link SimulationSession}).
 *
 * @author Matteo Belenchia, Michele Loreti
 */
public abstract class SimulationManager<S extends State> {


    protected final BlockingQueue<SimulationTask<S>> pendingTasks = new LinkedBlockingQueue<>();
    private final Consumer<Trajectory<S>> trajectoryConsumer;
    private final LinkedList<Long> executionTime = new LinkedList<>();
    private final SwingPropertyChangeSupport pcs = new SwingPropertyChangeSupport(this, true);
    private int runningTasks = 0;
    private RandomGenerator random;
    private boolean running = true;

    /**
     * Creates a new simulation manager.
     */
    public SimulationManager(RandomGenerator random, Consumer<Trajectory<S>> consumer) {
        this.random = random;
        this.trajectoryConsumer = consumer;
    }

    /**
     * Starts the handling of the tasks to be executed to complete the simulation
     */
    protected abstract void startTasksHandling();

    /**
     * Executes a simulation unit
     *
     * @param unit unit to be executed
     */
    public synchronized void simulate(SimulationUnit<S> unit) {
        if (!isRunning()) {
            throw new IllegalStateException();
        }
        add(new SimulationTask<>(random, unit));
    }

    /**
     * Add a simulationTask to the tasks to be executed
     *
     * @param simulationTask tasks to be added
     */
    private synchronized void add(SimulationTask<S> simulationTask) {
        pendingTasks.add(simulationTask);
        queueModified();
        notifyAll();
    }

    /**
     * Reschedules the task given in input
     *
     * @param simulationTask task to reschedule
     */
    protected synchronized void reschedule(SimulationTask<S> simulationTask) {
        runningTasks--;
        runningTasksModified();
        add(simulationTask);
    }

    /**
     * Reschedules all the tasks given in input
     *
     * @param tasks tasks to reschedule
     */
    protected synchronized void rescheduleAll(Collection<? extends SimulationTask<S>> tasks) {
        runningTasks -= tasks.size();
        runningTasksModified();
        addAll(tasks);
    }

    /**
     * Adds all the given tasks to the tasks to be executed
     *
     * @param tasks tasks to be added
     */
    private synchronized void addAll(Collection<? extends SimulationTask<S>> tasks) {
        pendingTasks.addAll(tasks);
        queueModified();
        notifyAll();
    }

    /**
     * Signal that the queue has been modified to listeners
     */
    private void queueModified() {
        propertyChange("waitingTasks", pendingTasks.size());
    }

    /**
     * Signal that the running tasks have been modified to listeners
     */
    private void runningTasksModified() {
        propertyChange("tasks", runningTasks);
    }

    /**
     * Handles the trajectory given in input
     *
     * @param trj trajectory to be handled
     */
    protected synchronized void handleTrajectory(Trajectory<S> trj) {
        this.executionTime.add(trj.getGenerationTime());
        trajectoryConsumer.accept(trj);
        runningTasks--;
        propertyChange("progress", executionTime.size());
        propertyChange("runtime", trj.getGenerationTime());
        runningTasksModified();
        notifyAll();
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
        runningTasksModified();
        queueModified();
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
     * * Gets the next n tasks to execute
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
        runningTasksModified();
        queueModified();
        return tasks;
    }

    public synchronized boolean isRunning() {
        return running;
    }

    protected synchronized void setRunning(boolean flag) {
        this.running = flag;
        notifyAll();
    }

    public void shutdown() throws InterruptedException {
        setRunning(false);
        join();
    }

    protected abstract void join() throws InterruptedException;

    protected Consumer<Trajectory<S>> getConsumer() {
        return trajectoryConsumer;
    }

    protected RandomGenerator getRandom() {
        return random;
    }

    protected List<Long> getExecutionTimes() {
        return executionTime;
    }

    protected boolean hasTasks() {
        return !pendingTasks.isEmpty();
    }

    protected int getRunningTasks() {
        return runningTasks;
    }

    public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(property, listener);
    }

    protected void propertyChange(String property, Object value) {
        pcs.firePropertyChange(property, null, value);
    }
}
