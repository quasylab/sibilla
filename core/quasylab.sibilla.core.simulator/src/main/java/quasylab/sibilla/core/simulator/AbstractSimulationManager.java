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

import org.apache.commons.math3.random.RandomGenerator;
import quasylab.sibilla.core.past.State;

import java.beans.PropertyChangeEvent;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class AbstractSimulationManager<S extends State> implements SimulationManager<S> {

    private final RandomGenerator random;
    private final SimulationMonitor monitor;
    private final Consumer<Trajectory<S>> trajectoryConsumer;
    private final LinkedList<Long> executionTime = new LinkedList<>();
    private int counter = 0;
    private boolean running = true;

    public AbstractSimulationManager(RandomGenerator random, SimulationMonitor monitor, Consumer<Trajectory<S>> trajectoryConsumer) {
        this.random = random;
        this.monitor = monitor;
        this.trajectoryConsumer = trajectoryConsumer;
        if (this.monitor != null) {
            this.monitor.registerPropertyChangeListener(this::manageSimulationMonitorEvent);
        }
    }

    /**
     * Handles the trajectory given in input
     *
     * @param trj trajectory to be handled
     */
    protected synchronized void handleTrajectory(Trajectory<S> trj) {
        this.executionTime.add(trj.getGenerationTime());
        trajectoryConsumer.accept(trj);
    }

    private void manageSimulationMonitorEvent(PropertyChangeEvent propertyChangeEvent) {
        if (propertyChangeEvent.getPropertyName().equals(SimulationMonitor.CANCELLED)) {
            if (getMonitor().isCancelled()) {
                try {
                    this.shutdown();
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                    //TODO: Add here Log message.
                }
            }
        }
    }



    @Override
    public synchronized void simulate(SimulationUnit<S> unit) {
        if (!isRunning()) {
            throw new IllegalStateException();
        }
        counter++;
        handleTask(new SimulationTask<>(counter,random,unit));
    }

    protected abstract void handleTask(SimulationTask<S> simulationTask);

    public SimulationMonitor getMonitor() {
        return monitor;
    }

    public int computedTrajectories() {
        return executionTime.size();
    }

    public double averageExecutionTime() {
        return executionTime.stream().collect(Collectors.averagingDouble(l -> l.doubleValue()));
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
    }

    protected void notifyMonitorStartInteration(int iterationIndex) {
        SimulationMonitor monitor = getMonitor();
        if (monitor != null) {
            monitor.startIteration(iterationIndex);
        }
    }

    protected void notifyMonitorEndInteration(int iterationIndex) {
        SimulationMonitor monitor = getMonitor();
        if (monitor != null) {
            monitor.endIteration(iterationIndex);
        }
    }

}
