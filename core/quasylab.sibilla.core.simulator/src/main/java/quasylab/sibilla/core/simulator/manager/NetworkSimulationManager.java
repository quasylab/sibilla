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
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
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

import quasylab.sibilla.core.simulator.SimulationTask;
import quasylab.sibilla.core.simulator.Trajectory;

/**
 * @author belenchia
 *
 */

import quasylab.sibilla.core.simulator.sampling.SamplingFunction;
import quasylab.sibilla.core.simulator.serialization.ClassBytesLoader;
import quasylab.sibilla.core.simulator.serialization.SerializationType;
import quasylab.sibilla.core.simulator.serialization.Serializer;
import quasylab.sibilla.core.simulator.server.ComputationResult;
import quasylab.sibilla.core.simulator.ui.SimulationView;

public class NetworkSimulationManager<S> implements SimulationManager<S> {
    private Map<Serializer, ServerState> servers = Collections.synchronizedMap(new HashMap<>());
    private final String modelName;
    private BlockingQueue<Serializer> serverQueue;
    private ExecutorService executor;
    private int workingServers = 0;
    private final SwingPropertyChangeSupport pcs = new SwingPropertyChangeSupport(this, true);
    private BlockingQueue<SimulationSession<S>> sessions = new LinkedBlockingQueue<>();
    //private final SerializationType serialization; //Default // FST

    public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(property, listener);
    }

    public NetworkSimulationManager(InetAddress[] servers, int[] ports, String modelName, SerializationType[] serialization)
            throws UnknownHostException, IOException {
        this.modelName = modelName;
        executor = Executors.newCachedThreadPool();
        for (int i = 0; i < servers.length; i++) {
            Serializer server = Serializer.createSerializer(new Socket(servers[i].getHostAddress(), ports[i]), serialization[i]);
            this.servers.put(server, new ServerState(server));
            try {
                initConnection(server);
            } catch (Exception e) {
                System.out.println("Error during server initialization, removing server...");
                this.servers.remove(server);
            }
        }
        serverQueue = new LinkedBlockingQueue<>(this.servers.keySet());
    }

    private void initConnection(Serializer server) throws Exception{
        byte[] classBytes = ClassBytesLoader.loadClassBytes(modelName);
        server.writeObject(modelName);
        //server.flush();
        server.writeObject(classBytes);
        //server.flush();
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
        Serializer server = findServer();
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

    private synchronized Serializer findServer() {
        return serverQueue.poll();
    }

    private void manageResult(ComputationResult<S> value, Throwable error, SimulationSession<S> session, List<SimulationTask<S>> tasks, Serializer server){
        if(error!=null){
            //timeout occurred, contact server
            Serializer newServer;
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
        runSelectedSession(nextSession); 
    }

    private  Serializer manageTimeout(Serializer server, SimulationSession<S> session){
        Serializer pingServer = null;
        ServerState removedState = null;
        try {
            removedState = servers.remove(server); // get old state, remove old server from map
            removedState.timedout();  // mark server as timed out and update GUI
            propertyChange("servers"+session.toString(), new String[]{server.getSocket().getInetAddress().getHostAddress()+":"+server.getSocket().getPort(), removedState.toString()});
            pingServer = Serializer.createSerializer(new Socket(server.getSocket().getInetAddress().getHostAddress(), server.getSocket().getPort()), SerializationType.getType(server)); // start new connection
            pingServer.getSocket().setSoTimeout(5000); // set 5 seconds timeout on read operations
            initConnection(pingServer); // initialize connection sending model data
            pingServer.writeObject("PING"); // send ping request
            //pingServer.flush();
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

    private synchronized void manageTask(SimulationSession<S> session, ComputationResult<S> result) {
        session.incrementReach(result.getReach());
        for (Trajectory<S> trajectory : result.getResults()) {
            doSample(session.getSamplingFunction(), trajectory);
            session.taskCompleted();
            propertyChange("progress"+session.toString(), session.getExpectedTasks());
        }
        workingServers--;
    }

    private synchronized void runSelectedSession(SimulationSession<S> session) {
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
                state.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private  ComputationResult<S> send(NetworkTask<S> networkTask, Serializer server){
        ComputationResult<S> result;
        long elapsedTime;
        ServerState state = servers.get(server);
        
        try {
            server.writeObject("TASK");
            //server.flush();
            server.writeObject(networkTask);
            //server.flush();

            elapsedTime = System.nanoTime();

            server.getSocket().setSoTimeout((int)(state.getTimeout() / 1000000));

            @SuppressWarnings("unchecked")
            ComputationResult<S> receivedResult = (ComputationResult<S>) server.readObject();
            
            elapsedTime = System.nanoTime() - elapsedTime;

            state.update(elapsedTime, receivedResult.getResults().size());

            result = receivedResult;  
        }catch(SocketTimeoutException e){

            throw new RuntimeException();

        } catch (Exception e) {

            throw new RuntimeException();
        }
        return result;
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
 

    private void propertyChange(String property, Object value){
        pcs.firePropertyChange(property, null, value);
    }

}