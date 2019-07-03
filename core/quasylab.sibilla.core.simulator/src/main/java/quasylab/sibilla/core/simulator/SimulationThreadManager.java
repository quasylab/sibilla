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

/**
 * @author belenchia
 *
 */
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import quasylab.sibilla.core.simulator.sampling.SamplingFunction;

public class SimulationThreadManager<S> implements SimulationManager<S> {
    ExecutorService executor = Executors.newCachedThreadPool();
    ExecutorCompletionService<Trajectory<S>> completionExecutor = new ExecutorCompletionService<>(executor);
    List<SimulationTask<S>> tasks = new LinkedList<>();
    int nTasks;
    int taskCounter = 0;
    SamplingFunction<S> sampling_function;
    LinkedList<SimulationTask<S>> waitingTasks = new LinkedList<>();
    Timer scheduler = new Timer();
    List<Boolean> reach = new LinkedList<>();

    public SimulationThreadManager(int nTasks) {
        this.nTasks = nTasks;
        scheduler.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                checkExecution();
            }
        }, 0L, 1L);
    }

    private synchronized void checkExecution() {
        Future<Trajectory<S>> result;
        while ((result = completionExecutor.poll()) != null) {
            // System.out.println(taskCounter + " "+ waitingTasks.size());
            try {
                if (sampling_function != null) {
                    result.get().sample(sampling_function);
                }
                taskCounter--;
                SimulationTask<S> nextTask = waitingTasks.poll();
                if (nextTask != null) {
                    run(nextTask);
                }else{
                    this.notify();
                }
            } catch (InterruptedException | ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setSampling(SamplingFunction<S> sampling_function) {
        this.sampling_function = sampling_function;
    }

    /*
     * @Override public void addTask(SimulationTask<S> task) { tasks.put(nTasks++,
     * task); }
     * 
     * @Override public void runTasks(SamplingFunction<S> sampling_function) {
     * this.sampling_function = sampling_function; List<Callable<Object>>
     * callableTasks =
     * tasks.entrySet().stream().map(Map.Entry::getValue).map(Executors::callable).
     * collect(Collectors.toList()); try { executor.invokeAll(callableTasks); }
     * catch (InterruptedException e) { // TODO Auto-generated catch block
     * e.printStackTrace(); } sampleTasks(sampling_function);
     * printTimingInformation(); clear(); }
     */
    @Override
    public void run(SimulationTask<S> task) {
        if (taskCounter < nTasks) {
            taskCounter++;
            tasks.add(task);
            completionExecutor.submit(task);
        } else {
            waitingTasks.add(task);
        }
    }

    @Override
    public synchronized void waitTermination() {

        if (waitingTasks.size() > 0) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        this.printTimingInformation();
    }
/*
    public List<Boolean> reach(){
        List<Callable<Object>> callableTasks = tasks.entrySet().stream().map(Map.Entry::getValue).map(Executors::callable).collect(Collectors.toList());
        try {
            executor.invokeAll(callableTasks);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        List<Boolean> result = tasks.entrySet().stream().map(Map.Entry::getValue).map(SimulationTask::reach).collect(Collectors.toList());
        printTimingInformation();
        clear();
        return result;
    }
*/


    /**
     * Clears the hash map
     */
    private void clear(){
        tasks.clear();
        nTasks = 0;
    }

    /**
     * Prints timing information stored in the hash map's threads
     */
    private void printTimingInformation(){
        System.out.println();
        for(int i = 0; i < tasks.size(); i++){
            System.out.println("Task " + i +  " Elapsed Time: " + tasks.get(i).getElapsedTime() + "ns");
        }
    }

    @Override
    public long reach() {
        return tasks.stream().filter(task -> task.reach() == true).count();
    }
    
}