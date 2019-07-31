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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import quasylab.sibilla.core.simulator.sampling.SamplingFunction;

/**
 * @author belenchia
 *
 */
public class ThreadSimulationManager<S> implements SimulationManager<S> {
    private ExecutorService executor;
    private LinkedList<SimulationTask<S>> tasks = new LinkedList<>();
    private final int concurrentTasks;
    private int runningTasks = 0;
    //private LinkedList<SimulationTask<S>> waitingTasks = new LinkedList<>();
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private BlockingQueue<SimulationSession<S>> sessions = new LinkedBlockingQueue<>();

    public ThreadSimulationManager(int concurrentTasks) {
        this.concurrentTasks = concurrentTasks;
        executor = Executors.newCachedThreadPool();
    }

    @Override
    public SimulationSession<S> newSession(int expectedTasks, SamplingFunction<S> sampling_function) {
        SimulationSession<S> newSession = new SimulationSession<S>(expectedTasks, sampling_function);
        sessions.add(newSession);
        new SimulationView<>(newSession, this);
        return newSession;
    }

    private void doSample(SamplingFunction<S> sampling_function, Trajectory<S> trajectory) {
        if (sampling_function != null) {
            trajectory.sample(sampling_function);
        }
    }

    // waits for all tasks to end, then prints timing information to file
    private void terminate(SimulationSession<S> session) {
        try {
            pcs.firePropertyChange("end"+session.toString(), null, "");
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
        pcs.firePropertyChange("progress"+session.toString(), session.getExpectedTasks(), session.taskCompleted());
        //pcs.firePropertyChange("runtime", null, tasks);
        runningTasks--;

        ////// pick next task
        SimulationTask<S> nextTask = null;
        SimulationSession<S> nextSession = null;
        for(int i = 0; i < sessions.size(); i++){
            nextSession = sessions.poll();
            nextTask = nextSession.getQueue().poll();
            if(nextTask != null){  // session is incomplete and has tasks to execute
                sessions.add(nextSession);
                break;
            }
            else if(nextSession.getExpectedTasks() > 0){ // session is incomplete but no tasks to execute
                sessions.add(nextSession);
            }else if (isCompleted(nextSession)){  // session is complete with no tasks still running
                this.notifyAll();
            }
        }
        //////////
        pcs.firePropertyChange("waitingTasks"+session.toString(), null, nextSession.getQueue().size());
        if (nextTask != null) {
            run(nextSession, nextTask);
        } /*else if (isCompleted(nextSession)) {
            this.notify();
        }*/
    }

    private synchronized boolean isCompleted(SimulationSession<S> session) {
        return (runningTasks + session.getExpectedTasks() == 0);
    }

    // runs a new task if below task limit, else adds to queue
    @Override
    public synchronized void run(SimulationSession<S> session, SimulationTask<S> task) {
        if (runningTasks < concurrentTasks) {
            pcs.firePropertyChange("threads"+session.toString(), runningTasks, ++runningTasks);
            tasks.add(task);
            CompletableFuture.supplyAsync(task, executor)
                              .whenComplete((value, error) -> showThreadRuntime(session, task))
                            .thenAccept((trajectory) -> this.manageTask(session, trajectory));
        } else {
            session.getQueue().add(task);
            pcs.firePropertyChange("waitingTasks"+session.toString(), null, session.getQueue().size());
        }
    }

    private synchronized void showThreadRuntime(SimulationSession<S> session, SimulationTask<S> task){
        pcs.firePropertyChange("runtime"+session.toString(), null, task.getElapsedTime());
        System.out.println(session);
    }

    // waiting until executor is shutdown
    @Override
    public synchronized void waitTermination(SimulationSession<S> session) throws InterruptedException {
        while (!isCompleted(session)) {
            this.wait();
        }
        terminate(session);
        // executor.shutdown(); // only when recording time
    }

    private void printTimingInformation(PrintStream out) {
        LongSummaryStatistics statistics = tasks.stream().map(x -> x.getElapsedTime()).mapToLong(Long::valueOf)
                .summaryStatistics();
        out.println(concurrentTasks + ";" + ((ThreadPoolExecutor) executor).getPoolSize() + ";"
                + statistics.getAverage() + ";" + statistics.getMax() + ";" + statistics.getMin());
    }

    

    @Override
    public long reach() {
        return tasks.stream().filter(task -> task.reach() == true).count();
    }

    @Override
    public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(property, listener);
    }
    
}