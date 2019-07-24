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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author belenchia
 *
 */

import quasylab.sibilla.core.simulator.sampling.SamplingFunction;

public class NetworkSimulationManager<S> implements SimulationManager<S> {
    private Map<Socket, ServerState> servers = new ConcurrentHashMap<>();
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

<<<<<<< HEAD
    private void checkTimeout(){
        List<Socket> toRemove = new LinkedList<>();
        while(!isTerminated){
            for(Map.Entry<Socket,ServerState> entry : servers.entrySet()){
                ServerState state = entry.getValue();
                if(state.isRunning() && state.getTimeout() > 0 && state.isTimeout()){
                    System.out.println("Elapsed time: " + state.getElapsedTime() +" Timeout value: " + state.getTimeout()+ " estimatedRTT: "+state.estimatedRTT+" devRTT: "+state.devRTT);
                    toRemove.add(entry.getKey());
                    System.out.println("removed server");
                }
            }
=======
            ObjectOutputStream oos = this.servers.get(server).getObjectOutputStream();
>>>>>>> dee81bbea591c9bbf0f1c92ea95046a3b6f792ad

            byte[] toSend = ClassBytesLoader.loadClassBytes(modelName);
            oos.writeObject(modelName);
            oos.writeObject(toSend);
        }
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
        run(session, new LinkedList<>(Arrays.asList(task)));
    }

    private synchronized void run(SimulationSession<S> session, List<SimulationTask<S>> tasks) {
        Socket server = findServer();
        run(session, tasks, server);
    }

    private synchronized void run(SimulationSession<S> session, List<SimulationTask<S>> tasks, Socket server) {
        if (server != null) {
            NetworkTask<S> networkTask = new NetworkTask<S>(tasks);
            workingServers++;
            servers.get(server).startRunning();
            CompletableFuture.supplyAsync(() -> send(networkTask, server), executor)
                    .thenAccept((trajectory) -> this.manageTask(session, trajectory))
                    .thenRun(() -> nextRun(session, server));
            pcs.firePropertyChange("servers", null, this.servers);
        } else {
            waitingTasks.addAll(tasks);
            pcs.firePropertyChange("waitingTasks", null, waitingTasks.size());
        }
    }

    private synchronized Socket findServer() {
        return servers.keySet().stream().filter(x -> !servers.get(x).isRunning()).findFirst().orElse(null);
    }

    private synchronized void manageTask(SimulationSession<S> session, List<Trajectory<S>> trajectories) {
        for (Trajectory<S> trajectory : trajectories) {
            doSample(session.getSamplingFunction(), trajectory);
            pcs.firePropertyChange("progress", session.getExpectedTasks(), session.taskCompleted());
        }
        workingServers--;
    }

    private synchronized void nextRun(SimulationSession<S> session, Socket server) {
        ServerState serverState;
        if ( !waitingTasks.isEmpty() && (serverState = servers.get(server)) != null) {
            int acceptableTasks = serverState.getTasks();
            List<SimulationTask<S>> nextTasks = getWaitingTasks(acceptableTasks);
            if (serverState.canCompleteTask(nextTasks.size())) {
                run(session, nextTasks, server);
            } else {
                try {
                    throw new Exception("Submitted task requires too much time!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } else if(isCompleted(session)){
            closeStreams();
            this.notify();
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
<<<<<<< HEAD
            //System.out.println(waitingTasks.size());       
=======
            state.printState();   
            if(state.isTimeout()) {
                servers.remove(server);
                //System.out.println("removed server" + server + " elapsedTime: "+state.getElapsedTime() + " Timeout: "+state.getTimeout());
            }      
>>>>>>> dee81bbea591c9bbf0f1c92ea95046a3b6f792ad


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