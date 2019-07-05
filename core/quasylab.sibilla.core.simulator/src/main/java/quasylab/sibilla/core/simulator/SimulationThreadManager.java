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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import quasylab.sibilla.core.simulator.sampling.SamplingFunction;
/**
 * @author belenchia
 *
 */
public class SimulationThreadManager<S> implements SimulationManager<S> {
    ExecutorService executor;
    BlockingQueue<Future<Trajectory<S>>> futures;
    ExecutorCompletionService<Trajectory<S>> completionExecutor;
    List<SimulationTask<S>> tasks = new LinkedList<>();
    int nTasks, expectedTasks, taskCounter = 0;
    SamplingFunction<S> sampling_function;
    LinkedList<SimulationTask<S>> waitingTasks = new LinkedList<>();
    List<Boolean> reach = new LinkedList<>();

    public SimulationThreadManager(int nTasks) {
        this.nTasks = nTasks;
        executor = Executors.newCachedThreadPool();
        futures = new LinkedBlockingQueue<>();
        completionExecutor = new ExecutorCompletionService<>(executor, futures);
    }

    private void doSample(Trajectory<S> trajectory) {
        if (sampling_function != null) {
            trajectory.sample(sampling_function);
        }
    }

    private void terminate() {
        try {
            executor.awaitTermination(60000, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            this.printTimingInformation( new PrintStream(new FileOutputStream("thread_data.data", true)));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }



    private synchronized void manageTask(Trajectory<S> trajectory) {
        doSample(trajectory);
        taskCounter--;
        expectedTasks--;
        SimulationTask<S> nextTask = waitingTasks.poll();
        if (nextTask != null) {
            run(nextTask);
        } else if (expectedTasks == 0) {
            executor.shutdown();
        }
    }

    @Override
    public void init(SamplingFunction<S> sampling_function, int expectedTasks) {
        this.sampling_function = sampling_function;
        this.expectedTasks = expectedTasks;
    }

    @Override
    public void run(SimulationTask<S> task) {
        if (taskCounter < nTasks) {
            taskCounter++;
            tasks.add(task);
            CompletableFuture.supplyAsync(task, executor).thenAccept(this::manageTask);
        } else {
            waitingTasks.add(task);
        }
    }

    @Override
    public void waitTermination() {
        while(executor.isShutdown() == false);
        terminate();
    }

    private void printTimingInformation(PrintStream out){
        LongSummaryStatistics statistics = tasks.stream().map(x -> x.getElapsedTime()).mapToLong(Long::valueOf).summaryStatistics();
        out.println(nTasks +";" + statistics.getAverage() + ";" + statistics.getMax() +";" + statistics.getMin());
    }

    @Override
    public long reach() {
        return tasks.stream().filter(task -> task.reach() == true).count();
    }
    
}