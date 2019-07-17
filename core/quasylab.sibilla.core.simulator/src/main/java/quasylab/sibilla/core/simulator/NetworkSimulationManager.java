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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author belenchia
 *
 */

import quasylab.sibilla.core.simulator.sampling.SamplingFunction;

public class NetworkSimulationManager<S> implements SimulationManager<S> {
    private final List<Socket> servers = new LinkedList<>();
    private ExecutorService executor;
    private int workingServers = 0;
    private boolean isRunning = false;

    public NetworkSimulationManager(InetAddress[] servers, int[] ports) {
        executor = Executors.newCachedThreadPool();
        for (int i = 0; i < servers.length; i++) {
            try {
                this.servers.add(new Socket(servers[i].getHostAddress(), ports[i]));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public SimulationSession<S> newSession(int expectedTasks, SamplingFunction<S> sampling_function) {
        return new SimulationSession<S>(expectedTasks, sampling_function);
    }

    private synchronized boolean isCompleted(SimulationSession<S> session) {
		return (workingServers+session.getExpectedTasks()==0);
    }

    @Override
    public void run(SimulationSession<S> session, SimulationTask<S> task) {
        if(isRunning)
            return;
        NetworkTask<S> networkTask = new NetworkTask<S>(task, session.getExpectedTasks());
        for( Socket server : servers){
            workingServers++;
            CompletableFuture.supplyAsync(() -> send(networkTask, server), executor).thenAccept((trajectory) -> this.manageTask(session, trajectory));
        }
        isRunning = true;
    }

    private synchronized void manageTask(SimulationSession<S> session, List<Trajectory<S>> trajectories){
        for(Trajectory<S> trajectory : trajectories){
            doSample(session.getSamplingFunction(), trajectory);
            session.taskCompleted();
        }
        workingServers--;
        System.out.println("Server finished running!");
        if(isCompleted(session)){
            this.notify();
        }
    }

    private List<Trajectory<S>> send(NetworkTask<S> networkTask, Socket server){
        ObjectOutputStream oos;
        ObjectInputStream ois;
        List<Trajectory<S>> trajectories = null;
        
        try {
            oos = new ObjectOutputStream(server.getOutputStream());
            ois = new ObjectInputStream(server.getInputStream());

            oos.writeObject(networkTask);

            @SuppressWarnings("unchecked")
            List<Trajectory<S>> result = (List<Trajectory<S>>) ois.readObject();

            trajectories = result;

        } catch (IOException | ClassNotFoundException e) {

            e.printStackTrace();
        }
        return trajectories;
    }

    private void doSample(SamplingFunction<S> sampling_function, Trajectory<S> trajectory) {
        if (sampling_function != null) {
            trajectory.sample(sampling_function);
        }
    }


    @Override
    public synchronized void waitTermination(SimulationSession<S> session) throws InterruptedException {
        while (!isCompleted(session)) {
            this.wait();
        } 
        System.out.println("Completed");
    }
 
    @Override
    public long reach() {
        return 0;
    }


}