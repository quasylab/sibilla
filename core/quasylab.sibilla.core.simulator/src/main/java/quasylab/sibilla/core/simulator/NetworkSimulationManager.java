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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author belenchia
 *
 */

import quasylab.sibilla.core.simulator.sampling.SamplingFunction;

public class NetworkSimulationManager<S> implements SimulationManager<S> {
    private Map<Socket, ServerState> servers = Collections.synchronizedMap(new HashMap<>());
    private final String modelName;
    private BlockingQueue<Socket> serverQueue;
    private ExecutorService executor;
    private int workingServers = 0;
    private BlockingQueue<SimulationTask<S>> waitingTasks = new LinkedBlockingQueue<>();
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private int c = 0, r = 0;

	public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(property, listener);
	}

    public NetworkSimulationManager(InetAddress[] servers, int[] ports, String modelName)
            throws UnknownHostException, IOException {
        this.modelName = modelName;
        executor = Executors.newCachedThreadPool();
        for (int i = 0; i < servers.length; i++) {
            Socket server = new Socket(servers[i].getHostAddress(), ports[i]);
            this.servers.put(server, new ServerState(server));
            ObjectOutputStream oos = this.servers.get(server).getObjectOutputStream();
            initConnection(server, oos);
        }
        serverQueue = new LinkedBlockingQueue<>(this.servers.keySet());
    }

    private void initConnection(Socket server, ObjectOutputStream oos) throws UnknownHostException, IOException {

            byte[] toSend = ClassBytesLoader.loadClassBytes(modelName);
            oos.writeObject(modelName);
            oos.writeObject(toSend);
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
        //System.out.println("map size: "+servers.size()+" queue size: "+serverQueue.size());
        Socket server = findServer();
        List<SimulationTask<S>> toRun;
        if (server != null) {
            ServerState serverState = servers.get(server);
            if(tasks.isEmpty()){ //get from waiting tasks
                int acceptableTasks = serverState.getExpectedTasks();
                toRun = getWaitingTasks(acceptableTasks);
                if (!serverState.canCompleteTask(toRun.size())) {
                    System.out.println("canCompleteFired");
                    waitingTasks.addAll(toRun);
                    toRun = getWaitingTasks(acceptableTasks / 2);
                }
            }else{ //get from params
                toRun = tasks;
            }
            final List<SimulationTask<S>> selectedTasks = toRun;
            NetworkTask<S> networkTask = new NetworkTask<S>(selectedTasks);
            workingServers++;
            serverState.startRunning();
            CompletableFuture.supplyAsync(() -> send(networkTask, server), executor)
                             .orTimeout((long)serverState.getTimeout(), TimeUnit.NANOSECONDS)
                             .whenComplete((value, error) -> timeoutHandler(value, error, session, selectedTasks, server));
        } else {
            waitingTasks.addAll(tasks);
        }
        pcs.firePropertyChange("waitingTasks", null, waitingTasks.size());
    }

    private synchronized Socket findServer() {
        return serverQueue.poll();
    }

    private synchronized void timeoutHandler(List<Trajectory<S>> value, Throwable error, SimulationSession<S> session, List<SimulationTask<S>> tasks, Socket server){
        if(error!=null){
            System.out.println("Timeout");
            //timeout occurred, contact server
            Socket newServer;
            if((newServer = manageTimeout(server))!= null){
                // server responded: abort computation, retry with half
                serverQueue.add(newServer); // add new server to queue, old server won't return                                                   
                System.out.println("Server refreshed");
            }else{
                // server not responding, remove server
                System.out.println("server deleted");
            }
            //both cases: 
            workingServers--;
            waitingTasks.addAll(tasks);
            r+=tasks.size();
        }else{
            //timeout not occurred, continue as usual
            //System.out.println("Nothing to report");
            c+=value.size();
            serverQueue.add(server); 
            manageTask(session, value);
            pcs.firePropertyChange("servers", null, servers.get(server));
        }
        nextRun(session); 

    }

    private synchronized Socket manageTimeout(Socket server){
        Socket pingServer = null;
        ServerState removedState = null;
        try {
            pingServer = new Socket(server.getInetAddress().getHostAddress(), server.getPort());
            pingServer.setSoTimeout(5000);
            removedState = servers.remove(server); // get old state, remove old server from map
            removedState.timedout();
            pcs.firePropertyChange("servers", null, removedState);
            removedState.forceExpiredTimeLimit();
            removedState.migrate(pingServer);
            ObjectOutputStream oos = removedState.getObjectOutputStream();
            ObjectInputStream ois = removedState.getObjectInputStream();
            initConnection(pingServer, oos);
            oos.writeObject("PING");
            String response = (String) ois.readObject();
            System.out.println(response);
            servers.put(pingServer, removedState);
        } catch (IOException | ClassNotFoundException e) {
            removedState.removed();
            pcs.firePropertyChange("servers", null, removedState);
            return null;
        }
        return pingServer;
    }

    private synchronized void manageTask(SimulationSession<S> session, List<Trajectory<S>> trajectories) {
        for (Trajectory<S> trajectory : trajectories) {
            doSample(session.getSamplingFunction(), trajectory);
            pcs.firePropertyChange("progress", session.getExpectedTasks(), session.taskCompleted());
        }
        workingServers--;
    }

    private synchronized void nextRun(SimulationSession<S> session) {
        if(servers.size()==0){
            System.out.println("No servers available");
            this.notify();
        }else if(!waitingTasks.isEmpty()){
            run(session,new LinkedList<>());
        }else if(isCompleted(session)){
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

            oos.writeObject("TASK");
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
            //serverQueue.add(server);     


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