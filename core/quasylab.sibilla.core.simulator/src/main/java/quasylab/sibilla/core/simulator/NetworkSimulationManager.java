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
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.event.SwingPropertyChangeSupport;

import org.nustaq.net.TCPObjectSocket;

/**
 * @author belenchia
 *
 */

import quasylab.sibilla.core.simulator.sampling.SamplingFunction;

public class NetworkSimulationManager<S> implements SimulationManager<S> {
    private Map<TCPObjectSocket, ServerState> servers = Collections.synchronizedMap(new HashMap<>());
    private final String modelName;
    private BlockingQueue<TCPObjectSocket> serverQueue;
    private ExecutorService executor;
    private int workingServers = 0;
    private final SwingPropertyChangeSupport pcs = new SwingPropertyChangeSupport(this, true);
    private BlockingQueue<SimulationSession<S>> sessions = new LinkedBlockingQueue<>();

    public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(property, listener);
    }

    public NetworkSimulationManager(InetAddress[] servers, int[] ports, String modelName)
            throws UnknownHostException, IOException {
        this.modelName = modelName;
        executor = Executors.newCachedThreadPool();
        for (int i = 0; i < servers.length; i++) {
            TCPObjectSocket server = new TCPObjectSocket(servers[i].getHostAddress(), ports[i]);
            this.servers.put(server, new ServerState(server));
            try {
                initConnection(server);
            } catch (Exception e) {
                this.servers.remove(server);
            }
        }
        serverQueue = new LinkedBlockingQueue<>(this.servers.keySet());
    }

    private void initConnection(TCPObjectSocket server) throws Exception{
        byte[] classBytes = ClassBytesLoader.loadClassBytes(modelName);
        server.writeObject(modelName);
        server.flush();
        server.writeObject(classBytes);
        server.flush();
    }

    @Override
    public SimulationSession<S> newSession(int expectedTasks, SamplingFunction<S> sampling_function, boolean enableGUI) {
        SimulationSession<S> newSession = new SimulationSession<S>(expectedTasks, sampling_function);
        sessions.add(newSession);
        if(enableGUI)
            new SimulationView<>(newSession, this);
        return newSession;
    }

    private synchronized boolean isCompleted(SimulationSession<S> session) {
        return (workingServers + session.getExpectedTasks() == 0);
    }

    @Override
    public void run(SimulationSession<S> session, SimulationTask<S> task) {
        session.getQueue().add(task);
        run(session);
    }

    private void run(SimulationSession<S> session) {
        TCPObjectSocket server = findServer();
        List<SimulationTask<S>> toRun;
        if (server != null){
            ServerState serverState = servers.get(server);
            int acceptableTasks = serverState.getExpectedTasks();
            toRun = getWaitingTasks(session, acceptableTasks);
            if (!serverState.canCompleteTask(toRun.size())) {
                session.getQueue().addAll(toRun);
                toRun = getWaitingTasks(session, acceptableTasks==1 ? 1 : acceptableTasks / 2);
            }
            final List<SimulationTask<S>> selectedTasks = toRun;
            NetworkTask<S> networkTask = new NetworkTask<S>(selectedTasks);
            workingServers++;
            CompletableFuture.supplyAsync(() -> send(networkTask, server), executor)
                             .whenComplete((value, error) -> manageResult(value, error, session, selectedTasks, server));
        }
        propertyChange("waitingTasks"+session.toString(), session.getQueue().size());
    }

    private synchronized TCPObjectSocket findServer() {
        return serverQueue.poll();
    }

    private void manageResult(List<Trajectory<S>> value, Throwable error, SimulationSession<S> session, List<SimulationTask<S>> tasks, TCPObjectSocket server){
        if(error!=null){
            //timeout occurred, contact server
            TCPObjectSocket newServer;
            if((newServer = manageTimeout(server, session))!= null){
                // server responded
                serverQueue.add(newServer); // add new server to queue, old server won't return                                                   
            }
            workingServers--;
            session.getQueue().addAll(tasks);
        }else{
            //timeout not occurred, continue as usual
            serverQueue.add(server); 
            manageTask(session, value);
            propertyChange("servers"+session.toString(), new String[]{server.getSocket().getInetAddress().getHostAddress()+":"+server.getSocket().getPort(), servers.get(server).toString()});
        }

        runNextSession();

    }

    private void runNextSession(){
        ///// select new session
        SimulationSession<S> nextSession = null;
        for(int i = 0; i < sessions.size(); i++){
            nextSession = sessions.poll();
            if(!nextSession.getQueue().isEmpty()){ // session is incomplete and has tasks to execute
                sessions.add(nextSession);
                break;
            }
            else if(nextSession.getExpectedTasks() > 0){ // session is incomplete but no tasks to execute
                sessions.add(nextSession);
            }else if(isCompleted(nextSession)){ // session is complete with no tasks still running
                terminateSession(nextSession);
            }
        }

        /////////////////////////
        nextRun(nextSession); 
    }

    private  TCPObjectSocket manageTimeout(TCPObjectSocket server, SimulationSession<S> session){
        TCPObjectSocket pingServer = null;
        ServerState removedState = null;
        try {
            removedState = servers.remove(server); // get old state, remove old server from map
            removedState.timedout();  // mark server as timed out and update GUI
            propertyChange("servers"+session.toString(), new String[]{server.getSocket().getInetAddress().getHostAddress()+":"+server.getSocket().getPort(), removedState.toString()});
            pingServer = new TCPObjectSocket(server.getSocket().getInetAddress().getHostAddress(), server.getSocket().getPort()); // start new connection
            pingServer.getSocket().setSoTimeout(5000); // set 5 seconds timeout on read operations
            initConnection(pingServer); // initialize connection sending model data
            pingServer.writeObject("PING"); // send ping request
            pingServer.flush();
            String response = (String) pingServer.readObject(); //wait for response
            if(!response.equals("PONG")){
                throw new IllegalStateException("Expected a different reply!");
            }
            // response received before time limit:
            removedState.forceExpiredTimeLimit(); // halve the task window
            removedState.migrate(pingServer); // change socket in serverstate
            servers.put(pingServer, removedState); //update hash map
        } catch (Exception e) {
            // time limit expired, or some other error happened
            removedState.removed(); //mark server as removed and update GUI
            propertyChange("servers"+session.toString(), new String[]{server.getSocket().getInetAddress().getHostAddress()+":"+server.getSocket().getPort(), removedState.toString()});
            return null;
        }
        return pingServer;
    }

    private synchronized void manageTask(SimulationSession<S> session, List<Trajectory<S>> trajectories) {
        for (Trajectory<S> trajectory : trajectories) {
            doSample(session.getSamplingFunction(), trajectory);
            session.taskCompleted();
            propertyChange("progress"+session.toString(), session.getExpectedTasks());
        }
        workingServers--;
    }

    private synchronized void nextRun(SimulationSession<S> session) {
        if(servers.size()==0){
            this.notifyAll();
        }else if(!session.getQueue().isEmpty()){
            run(session);
        }
    }

    private synchronized void terminateSession(SimulationSession<S> session){
        if(sessions.isEmpty()){
            closeStreams();
        }
        this.notifyAll();
    }

    private synchronized List<SimulationTask<S>> getWaitingTasks(SimulationSession<S> session, int n){
        List<SimulationTask<S>> fetchedTasks = new LinkedList<>();
        session.getQueue().drainTo(fetchedTasks,n);
        propertyChange("waitingTasks"+session.toString(), session.getQueue().size());
        return fetchedTasks;
    }

    private void closeStreams(){
        for( ServerState state: servers.values()){
            try {
                state.getServer().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private  List<Trajectory<S>> send(NetworkTask<S> networkTask, TCPObjectSocket server){
        List<Trajectory<S>> trajectories = new LinkedList<>();
        long elapsedTime;
        ServerState state = servers.get(server);
        
        try {
            server.writeObject("TASK");
            server.flush();
            server.writeObject(networkTask);
            server.flush();

            server.getSocket().setSoTimeout((int)(state.getTimeout() / 1000000));

            @SuppressWarnings("unchecked")
            ComputationResult<S> result = (ComputationResult<S>) server.readObject();
            
            trajectories = result.getResults();
            elapsedTime = result.getElapsedTime();

            state.update(elapsedTime, trajectories.size());  
        }catch(SocketTimeoutException e){

            throw new RuntimeException();

        } catch (Exception e) {

            throw new RuntimeException();
        }
        return trajectories;
    }


    
    private synchronized void doSample(SamplingFunction<S> sampling_function, Trajectory<S> trajectory) {
        if (sampling_function != null) {
            trajectory.sample(sampling_function);
        }
    }

    @Override
    public synchronized void waitTermination(SimulationSession<S> session) throws InterruptedException {
        while (!isCompleted(session)) {
            if(servers.size() == 0)
                break;
            this.wait();
        } 
        propertyChange("end"+session.toString(), "");
    }
 
    @Override
    public long reach() {
        return 0;
    }

    private void propertyChange(String property, Object value){
        pcs.firePropertyChange(property, null, value);
    }

}