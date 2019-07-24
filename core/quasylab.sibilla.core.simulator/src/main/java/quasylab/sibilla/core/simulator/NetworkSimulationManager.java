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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author belenchia
 *
 */

import quasylab.sibilla.core.simulator.sampling.SamplingFunction;

public class NetworkSimulationManager<S> implements SimulationManager<S> {
    private Map<Socket, ServerState> servers = new ConcurrentHashMap<>();
    private ConcurrentLinkedQueue<Socket> serverQueue;
    private ExecutorService executor;
    private int workingServers = 0;
    private LinkedList<SimulationTask<S>> waitingTasks = new LinkedList<>();
    private boolean isTerminated = false;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(property, listener);
	}

    public NetworkSimulationManager(InetAddress[] servers, int[] ports, String modelName)
            throws UnknownHostException, IOException {
        executor = Executors.newCachedThreadPool();
        for (int i = 0; i < servers.length; i++) {
            Socket server = new Socket(servers[i].getHostAddress(), ports[i]);
            this.servers.put(server, new ServerState(server));

            ObjectOutputStream oos = this.servers.get(server).getObjectOutputStream();

            byte[] toSend = ClassBytesLoader.loadClassBytes(modelName);
            oos.writeObject(modelName);
            oos.writeObject(toSend);
        }
        serverQueue = new ConcurrentLinkedQueue<>(this.servers.keySet());
    }

    @Override
    public SimulationSession<S> newSession(int expectedTasks, SamplingFunction<S> sampling_function) {
        new SimulationView<>(expectedTasks, this);
        return new SimulationSession<S>(expectedTasks, sampling_function);
    }

    private synchronized boolean isCompleted(SimulationSession<S> session) {
        return (workingServers + session.getExpectedTasks() == 0);
    }

    @Override
    public synchronized void run(SimulationSession<S> session, SimulationTask<S> task) {
        Socket server = findServer();
        run(session, new LinkedList<>(Arrays.asList(task)), server);
    }

    private synchronized void run(SimulationSession<S> session, List<SimulationTask<S>> tasks, Socket server) {
        if (server != null) {
            NetworkTask<S> networkTask = new NetworkTask<S>(tasks);
            workingServers++;
            ServerState state = servers.get(server);
            state.startRunning();
            CompletableFuture.supplyAsync(() -> send(networkTask, server), executor)
                             .orTimeout((long)state.getTimeout(),TimeUnit.NANOSECONDS)
                             .whenComplete((value, error) -> {
                                                                if(error!=null){
                                                                    //send still alive message
                                                                    waitingTasks.addAll(tasks);
                                                                    nextRun(session);
                                                                }else{
                                                                    manageTask(session, value);
                                                                    nextRun(session);
                                                                }
                                                            } 
                                            );
                                            
                    //.thenRun(() -> nextRun(session));//.orTimeout(timeout, unit);
            pcs.firePropertyChange("servers", null, servers.get(server));
        } else {
            waitingTasks.addAll(tasks);
        }
        pcs.firePropertyChange("waitingTasks", null, waitingTasks.size());
    }

    private synchronized Socket findServer() {
        //return servers.keySet().stream().filter(x -> !servers.get(x).isRunning()).findFirst().orElse(null);
        return serverQueue.poll();
    }

    private synchronized void manageTask(SimulationSession<S> session, List<Trajectory<S>> trajectories) {
        for (Trajectory<S> trajectory : trajectories) {
            doSample(session.getSamplingFunction(), trajectory);
            pcs.firePropertyChange("progress", session.getExpectedTasks(), session.taskCompleted());
        }
        workingServers--;
    }

    private synchronized void nextRun(SimulationSession<S> session) {
        Socket server = findServer();
        ServerState serverState;
        if ( !waitingTasks.isEmpty() && (serverState = servers.get(server)) != null) {
            int acceptableTasks = serverState.getTasks();
            List<SimulationTask<S>> nextTasks = getWaitingTasks(acceptableTasks);
            if (serverState.canCompleteTask(nextTasks.size())) {
                run(session, nextTasks, server);
            } else {
                waitingTasks.addAll(nextTasks);
                nextTasks = getWaitingTasks(acceptableTasks / 2);
                run(session, nextTasks, server);
            }

        } else if(isCompleted(session)){
            closeStreams();
            this.notify();
        } else {
            serverQueue.add(server);
        }
    }

    private synchronized List<SimulationTask<S>> getWaitingTasks(int n){
        List<SimulationTask<S>> fetchedTasks = new LinkedList<>();
        for(int i = 0; i < n; i++){
            SimulationTask<S> next = waitingTasks.poll();
            if(next != null)
                fetchedTasks.add(next);
            else
                break;
        }
        pcs.firePropertyChange("waitingTasks", null, waitingTasks.size());
        return fetchedTasks;
    }

    private void closeStreams(){
        for( ServerState state: servers.values()){
            try {
                state.getObjectInputStream().close();
                state.getObjectOutputStream().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        isTerminated = true;
    }

    private synchronized List<Trajectory<S>> send(NetworkTask<S> networkTask, Socket server){
        ObjectOutputStream oos;
        ObjectInputStream ois;
        List<Trajectory<S>> trajectories = new LinkedList<>();
        List<Long> timings = new LinkedList<>();
        ServerState state;
        
        try {

            oos = servers.get(server).getObjectOutputStream();
            ois = servers.get(server).getObjectInputStream();

            oos.writeObject(networkTask);

            @SuppressWarnings("unchecked")
            List<ComputationResult<S>> result = (List<ComputationResult<S>>) ois.readObject();

            for(ComputationResult<S> compResult : result){
                trajectories.add(compResult.getTrajectory());
                timings.add(compResult.getElapsedTime());
            }

            state = servers.get(server);
            state.stopRunning();
            state.update(timings);
            serverQueue.add(server);     


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