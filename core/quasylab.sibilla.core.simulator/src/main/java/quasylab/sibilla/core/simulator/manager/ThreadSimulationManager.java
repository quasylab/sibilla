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

package quasylab.sibilla.core.simulator.manager;

import java.beans.PropertyChangeListener;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.LongSummaryStatistics;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import javax.swing.event.SwingPropertyChangeSupport;

import quasylab.sibilla.core.simulator.SimulationTask;
import quasylab.sibilla.core.simulator.Trajectory;
import quasylab.sibilla.core.simulator.sampling.SamplingFunction;
import quasylab.sibilla.core.simulator.ui.SimulationView;

/**
 * @author belenchia
 *
 */
public class ThreadSimulationManager<S> implements SimulationManager<S> {
    private static final String loggingFile = "thread_data.data";
    private final ExecutorService executor;
    private final int concurrentTasks;
    private final SwingPropertyChangeSupport pcs = new SwingPropertyChangeSupport(this, true);
    private int runningTasks = 0;
    private Queue<SimulationSession<S>> sessions = new LinkedList<>();

    public ThreadSimulationManager(int concurrentTasks) {
        this.concurrentTasks = concurrentTasks;
        executor = Executors.newCachedThreadPool();
    }

    @Override
    public SimulationSession<S> newSession(int expectedTasks, SamplingFunction<S> sampling_function, boolean enableGUI) {
        SimulationSession<S> newSession = new SimulationSession<S>(expectedTasks, sampling_function);
        sessions.add(newSession);
        if(enableGUI)
            new SimulationView<>(newSession, this);
        return newSession;
    }

    private synchronized void doSample(SamplingFunction<S> sampling_function, Trajectory<S> trajectory) {
        if (sampling_function != null) {
            trajectory.sample(sampling_function);
        }
    }

    // waits for all tasks to end, then prints timing information to file
    private void terminate(SimulationSession<S> session) {
        try {
            propertyChange("end"+session.toString(), getTimingInformation(session));
            printTimingInformation(getTimingInformation(session), new PrintStream(new FileOutputStream(loggingFile, true)));
            printTimingInformation(getTimingInformation(session), System.out);
        } catch (FileNotFoundException e) {
        }
    }

    private void printTimingInformation(String str, PrintStream out){
        out.println(str);
    }

    private synchronized void runNextSession(){
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
        propertyChange("waitingTasks"+nextSession.toString(), nextSession.getQueue().size());
        if (nextTask != null) {
            run(nextSession, nextTask);
        }
    }

    // samples the trajectory, updates counters, then runs next task.
    // if no new tasks to run, shutdowns the executor
    private synchronized void manageResult(SimulationSession<S> session, Trajectory<S> trajectory) {
        doSample(session.getSamplingFunction(), trajectory);
        session.taskCompleted();
        runningTasks--;
        propertyChange("progress"+session.toString(), session.getExpectedTasks());
    }

    private synchronized boolean isCompleted(SimulationSession<S> session) {
        return (runningTasks + session.getExpectedTasks() == 0);
    }

    // runs a new task if below task limit, else adds to queue
    @Override
    public synchronized void run(SimulationSession<S> session, SimulationTask<S> task) {
        if (runningTasks < concurrentTasks) {
            runningTasks++;
            propertyChange("threads"+session.toString(), runningTasks);
            CompletableFuture.supplyAsync(task, executor)
                             .whenComplete((value, error) -> readTaskInformation(session, task))
                             .thenAccept((trajectory) -> manageResult(session, trajectory))
                             .thenRun(this::runNextSession);
        } else {
            session.getQueue().add(task);
            propertyChange("waitingTasks"+session.toString(), session.getQueue().size());
        }
    }

    private synchronized void readTaskInformation(SimulationSession<S> session, SimulationTask<S> task){
        long elapsedTime = task.getElapsedTime();
        if(task.reach()){
            session.incrementReach();
        }
        session.getTimeList().add(elapsedTime);
        propertyChange("runtime"+session.toString(), elapsedTime);
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

    private String getTimingInformation(SimulationSession<S> session) {
        LongSummaryStatistics statistics = session.getTimeList().stream().mapToLong(Long::valueOf)
                .summaryStatistics();
        return concurrentTasks + ";" + ((ThreadPoolExecutor) executor).getPoolSize() + ";"
                + statistics.getAverage() + ";" + statistics.getMax() + ";" + statistics.getMin();
    }
    

    @Override
    public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(property, listener);
    }

    private void propertyChange(String property, Object value){
        pcs.firePropertyChange(property, null, value);
    }
    
}