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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import quasylab.sibilla.core.simulator.sampling.SamplingFunction;

public class SimulationThreadManager<S> implements SimulationManager<S> {
    ExecutorService executor = Executors.newCachedThreadPool();
    Map<Integer, SimulationTask<S>> tasks = new HashMap<>();
    int nTasks = 0;
    SamplingFunction<S> sampling_function;

    @Override
    public void addTask(SimulationTask<S> task) {
        tasks.put(nTasks++, task);
    }

    @Override
    public void runTasks(SamplingFunction<S> sampling_function) {
        this.sampling_function = sampling_function;
        List<Callable<Object>> callableTasks = tasks.entrySet().stream().map(Map.Entry::getValue).map(Executors::callable).collect(Collectors.toList());
        try {
            executor.invokeAll(callableTasks);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        sampleTasks(sampling_function);
        printTimingInformation();
        clear();
    }

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

    private void sampleTasks(SamplingFunction<S> f) {
        for(Integer key : tasks.keySet()){
            Trajectory<S> trajectory = tasks.get(key).getTrajectory();
            if (f!=null) {
                trajectory.sample(f, key.intValue());
            }
        }

    }

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
        tasks.forEach((k, v) -> System.out.println("Task " + k +  " Elapsed Time: " + v.getElapsedTime() + "ns"));
    }
    
}