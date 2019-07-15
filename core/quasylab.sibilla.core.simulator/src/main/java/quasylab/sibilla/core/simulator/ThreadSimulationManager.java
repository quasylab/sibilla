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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import quasylab.sibilla.core.simulator.sampling.SamplingFunction;

/**
 * @author belenchia
 *
 */
public class ThreadSimulationManager<S> implements SimulationManager<S> {
    private ExecutorService executor;
    private List<SimulationTask<S>> tasks = new LinkedList<>();
    private final int concurrentTasks;
    private int runningTasks = 0;
    private LinkedList<SimulationTask<S>> waitingTasks = new LinkedList<>();

    public ThreadSimulationManager(int concurrentTasks) {
        this.concurrentTasks = concurrentTasks;
        executor = Executors.newCachedThreadPool();
    }

    public SimulationSession<S> newSession(int expectedTasks, SamplingFunction<S> sampling_function){
        return new SimulationSession<S>(expectedTasks, sampling_function);
    }

    private void doSample(SamplingFunction<S> sampling_function, Trajectory<S> trajectory) {
        if (sampling_function != null) {
            trajectory.sample(sampling_function);
        }
    }

    // waits for all tasks to end, then prints timing information to file
    private void terminate() {
        try {
            printTimingInformation(System.out);
            printTimingInformation(new PrintStream(new FileOutputStream("thread_data.data", true)));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    // samples the trajectory, updates counters, then runs next task.
    // if no new tasks to run, shutdowns the executor
    private synchronized void manageTask(SimulationSession<S> session, Trajectory<S> trajectory) {
        doSample(session.getSamplingFunction(), trajectory);
        runningTasks--;
        session.taskCompleted();
        SimulationTask<S> nextTask = waitingTasks.poll();
        if (nextTask != null) {
            run(session, nextTask);
        } else if (isCompleted(session.getExpectedTasks())){
            this.notify();
        }
    }

    private synchronized boolean isCompleted(int expectedTasks) {
		return (runningTasks+expectedTasks==0);
	}

	/*@Override
    public void init(SamplingFunction<S> sampling_function, int expectedTasks) {
        this.sampling_function = sampling_function;
        this.expectedTasks = expectedTasks;
    }*/

    // runs a new task if below task limit, else adds to queue
    @Override
    public synchronized void run(SimulationSession<S> session, SimulationTask<S> task) {
        if (runningTasks < concurrentTasks) {
            runningTasks++;
            tasks.add(task);
            CompletableFuture.supplyAsync(task, executor).thenAccept((trajectory) -> this.manageTask(session, trajectory));
        } else {
            waitingTasks.add(task);
        }
    }

    // busy waiting until executor is shutdown
    @Override
    public synchronized void waitTermination(SimulationSession<S> session) throws InterruptedException {
        // while(executor.isShutdown() == false);
        while (!isCompleted(session.getExpectedTasks())) {
            this.wait();
        } 
        terminate();
        //executor.shutdown(); // only when recording time
    }

    private void printTimingInformation(PrintStream out){
        LongSummaryStatistics statistics = tasks.stream().map(x -> x.getElapsedTime()).mapToLong(Long::valueOf).summaryStatistics();
        out.println(concurrentTasks +";"+((ThreadPoolExecutor) executor).getPoolSize()+";" + statistics.getAverage() + ";" + statistics.getMax() +";" + statistics.getMin());
    }

    @Override
    public long reach() {
        return tasks.stream().filter(task -> task.reach() == true).count();
    }
    
}