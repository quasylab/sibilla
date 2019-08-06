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
import java.net.SocketException;
import java.net.SocketTimeoutException;
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
import java.util.concurrent.TimeoutException;

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
    // private BlockingQueue<SimulationTask<S>> waitingTasks = new
    // LinkedBlockingQueue<>();
    private final SwingPropertyChangeSupport pcs = new SwingPropertyChangeSupport(this, true);    private int c = 0, r = 0;
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
            // ObjectOutputStream oos = this.servers.get(server).getObjectOutputStream();
            initConnection(server);
        }
        serverQueue = new LinkedBlockingQueue<>(this.servers.keySet());
    }

    private void initConnection(TCPObjectSocket server) {

        try {
            byte[] toSend = ClassBytesLoader.loadClassBytes(modelName);
            server.writeObject(modelName);
            server.writeObject(toSend);
            server.flush();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
    public synchronized void run(SimulationSession<S> session, SimulationTask<S> task) {
        session.getQueue().add(task);
        /*if(session.getExpectedTasks() != session.getQueue().size())
            return;*/
        run(session);
    }

    private synchronized void run(SimulationSession<S> session) {
        //System.out.println("map size: "+servers.size()+" queue size: "+serverQueue.size());
        TCPObjectSocket server = findServer();
        List<SimulationTask<S>> toRun;
        if (server != null){
            ServerState serverState = servers.get(server);
            int acceptableTasks = serverState.getExpectedTasks();
            toRun = getWaitingTasks(session, acceptableTasks);
            if (!serverState.canCompleteTask(toRun.size())) {
                System.out.println("canCompleteFired");
                session.getQueue().addAll(toRun);
                toRun = getWaitingTasks(session, acceptableTasks==1 ? 1 : acceptableTasks / 2);
            }
            final List<SimulationTask<S>> selectedTasks = toRun;
            NetworkTask<S> networkTask = new NetworkTask<S>(selectedTasks);
            workingServers++;
            //serverState.startRunning();
            CompletableFuture.supplyAsync(() -> send(networkTask, server), executor)
                             //.orTimeout((long)serverState.getTimeout(), TimeUnit.NANOSECONDS)
                             .whenComplete((value, error) -> timeoutHandler(value, error, session, selectedTasks, server));
        }
        propertyChange("waitingTasks"+session.toString(), session.getQueue().size());
    }

    private synchronized TCPObjectSocket findServer() {
        return serverQueue.poll();
    }

    private void timeoutHandler(List<Trajectory<S>> value, Throwable error, SimulationSession<S> session, List<SimulationTask<S>> tasks, TCPObjectSocket server){
        if(error!=null){
            System.out.println("Timeout");
            //timeout occurred, contact server
            TCPObjectSocket newServer;
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
            session.getQueue().addAll(tasks);
        }else{
            //timeout not occurred, continue as usual
            //System.out.println("Nothing to report");
            serverQueue.add(server); 
            manageTask(session, value);
            propertyChange("servers"+session.toString(), servers.get(server));
        }
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
                if(sessions.isEmpty()){
                    closeStreams();
                }
                this.notifyAll();
            }
        }

        /////////////////////////
        nextRun(nextSession); 

    }

    private TCPObjectSocket manageTimeout(TCPObjectSocket server){
        TCPObjectSocket pingServer = null;
        ServerState removedState = null;
        try {
            pingServer = new TCPObjectSocket(server.getSocket().getInetAddress().getHostAddress(), server.getSocket().getPort());
            pingServer.getSocket().setSoTimeout(5000);
            removedState = servers.remove(server); // get old state, remove old server from map
            removedState.timedout();
            propertyChange("servers", removedState);
            removedState.forceExpiredTimeLimit();
            removedState.migrate(pingServer);
            //ObjectOutputStream oos = removedState.getObjectOutputStream();
            //ObjectInputStream ois = removedState.getObjectInputStream();
            initConnection(pingServer);
            pingServer.writeObject("PING");
            String response = (String) pingServer.readObject();
            System.out.println(response);
            servers.put(pingServer, removedState);
        } catch (Exception e) {
            removedState.removed();
            propertyChange("servers", removedState);
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
            System.out.println("No servers available");
            this.notifyAll();
        }else if(!session.getQueue().isEmpty()){
            run(session);
        }/*else if(isCompleted(session)){
            closeStreams();
            this.notify();
        }*/
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

    private List<Trajectory<S>> send(NetworkTask<S> networkTask, TCPObjectSocket server){
        ObjectOutputStream oos;
        ObjectInputStream ois;
        List<Trajectory<S>> trajectories = new LinkedList<>();
        List<Long> timings = new LinkedList<>();
        ServerState state = servers.get(server);
        
        try {

            //oos = servers.get(server).getObjectOutputStream();
            //ois = servers.get(server).getObjectInputStream();

            server.writeObject("TASK");
            server.writeObject(networkTask);
            server.flush();

            server.getSocket().setSoTimeout((int)(state.getTimeout() / 1000000));

            @SuppressWarnings("unchecked")
            List<ComputationResult<S>> result = (List<ComputationResult<S>>) server.readObject();

            for(ComputationResult<S> compResult : result){
                trajectories.add(compResult.getTrajectory());
                timings.add(compResult.getElapsedTime());
            }

            //state.stopRunning();
            state.update(timings);  
            //System.out.println("updated"); 
        }catch(SocketTimeoutException e){

            System.out.println(state.getTimeout());

            throw new RuntimeException();

        } catch (IOException | ClassNotFoundException e) {

            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
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
            if(servers.size() == 0)
                break;
            this.wait();
        } 
        propertyChange("end"+session.toString(), "");
        System.out.println("Completed");
    }
 
    @Override
    public long reach() {
        return 0;
    }

    private void propertyChange(String property, Object value){
        pcs.firePropertyChange(property, null, value);
    }

}