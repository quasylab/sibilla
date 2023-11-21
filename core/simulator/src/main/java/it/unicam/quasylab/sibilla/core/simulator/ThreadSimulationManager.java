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
import it.unicam.quasylab.sibilla.core.simulator.sampling.SamplingHandler;
import org.apache.commons.math3.random.RandomGenerator;

import java.security.Provider;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * @author belenchia
 */

public class ThreadSimulationManager<S extends State> extends AbstractSimulationManager<S> {

    private static final Logger LOGGER = Logger.getLogger(ThreadSimulationManager.class.getName());
    private final ExecutorService executor;
    private int pendingTasks = 0;


    public static <S extends State> ThreadSimulationManager<S> getCachedTreadPoolSimulationManager(RandomGenerator random, SimulationMonitor monitor) {
        return new ThreadSimulationManager<>(Executors.newCachedThreadPool(), random, monitor);
    }

    public static <S extends State> ThreadSimulationManager<S> getFixedThreadPoolSimulationManager(int nThreads, RandomGenerator random, SimulationMonitor monitor) {
        return new ThreadSimulationManager<>(Executors.newFixedThreadPool(nThreads), random, monitor);
    }

    public ThreadSimulationManager(RandomGenerator random, SimulationMonitor monitor) {
        this(Executors.newCachedThreadPool(), random, monitor);
    }

    public ThreadSimulationManager(ExecutorService executor, RandomGenerator random, SimulationMonitor monitor) {
        super(random, monitor);
        this.executor = executor;
    }

    public static SimulationManagerFactory getThreadSimulationManagerFactory(Supplier<ExecutorService> executor) {
        return new SimulationManagerFactory() {
            @Override
            public <S extends State> SimulationManager<S> getSimulationManager(RandomGenerator random, SimulationMonitor monitor) {
                return new ThreadSimulationManager<>(executor.get(), random, monitor);
            }
        };
    }

    public static SimulationManagerFactory getFixedThreadSimulationManagerFactory(int n) {
        return getThreadSimulationManagerFactory(() -> Executors.newFixedThreadPool(n));
    }

    public static SimulationManagerFactory getCachedThreadSimulationManagerFactory() {
        return getThreadSimulationManagerFactory(Executors::newCachedThreadPool);
    }

    public static SimulationManagerFactory getWorkStealingPoolSimulationManagerFactory() {
        return getThreadSimulationManagerFactory(Executors::newWorkStealingPool);
    }


    @Override
    protected synchronized void handleTask(SimulationTask<S> simulationTask) {
        this.pendingTasks++;
        CompletableFuture.supplyAsync(simulationTask, executor).whenComplete(
                (t, e) -> {
                    if (e != null) {
                        LOGGER.warning(e.getLocalizedMessage());
                    }
                    taskCompleted(simulationTask);
                }
        );
    }

    private synchronized void taskCompleted(SimulationTask<S> simulationTask) {
        this.pendingTasks--;
        notifyAll();
    }


    @Override
    public synchronized int pendingTasks() {
        return pendingTasks;
    }

    @Override
    public synchronized void join() throws InterruptedException {
        while (isRunning() && pendingTasks() != 0) {
            wait();
        }
    }

    @Override
    public synchronized void shutdown() throws InterruptedException {
        super.shutdown();
        executor.shutdown();
    }
}