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
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;

public class NetworkSimulationManager extends SimulationManager {
	
    private Map<Socket, ServerState> servers = new ConcurrentHashMap<>();
    private ExecutorService executor;
    private int workingServers = 0;
    private boolean isTerminated = false;

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
    }


    protected <S> void runSimulation(RandomGenerator random, Consumer<Trajectory<S>> consumer, SimulationUnit<S> unit) {
    	deploy( new SimulationTask<>(random, unit), consumer );
    }

    public synchronized <S> void deploy(SimulationTask<S> task, Consumer<Trajectory<S>> consumer) {
    	deploy(new LinkedList<>(Arrays.asList(task)),consumer);
    }

    private synchronized <S> void deploy(List<SimulationTask<S>> tasks, Consumer<Trajectory<S>> consumer) {
        Socket server = findServer();
        deploy(tasks, consumer, server);
    }

    private synchronized <S> void deploy(List<SimulationTask<S>> tasks, Consumer<Trajectory<S>> consumer, Socket server) {
        if (server != null) {
            NetworkTask<S> networkTask = new NetworkTask<S>(tasks);
            workingServers++;
            servers.get(server).startRunning();
            CompletableFuture.supplyAsync(() -> send(networkTask, server), executor)
                    .thenAccept((trajectory) -> this.manageTask(consumer, trajectory));
        } else {
        	throw new NullPointerException(); //FIXME!!!
            //waitingTasks.addAll(tasks);
        }
    }

    private synchronized Socket findServer() {
        return servers.keySet().stream().filter(x -> !servers.get(x).isRunning()).findFirst().orElse(null);
    }

    private synchronized <S> void manageTask(Consumer<Trajectory<S>> consumer, List<Trajectory<S>> trajectories) {
        for (Trajectory<S> trajectory : trajectories) {
        	consumer.accept(trajectory);
        }
        workingServers--;
    }

//    private synchronized void nextRun(SimulationSession<S> session, Socket server) {
//        ServerState serverState;
//        if ( !waitingTasks.isEmpty() && (serverState = servers.get(server)) != null) {
//            int acceptableTasks = serverState.getTasks();
//            List<SimulationTask<S>> nextTasks = getWaitingTasks(acceptableTasks);
//            if (serverState.canCompleteTask(nextTasks.size())) {
//                run(session, nextTasks, server);
//            } else {
//                try {
//                    throw new Exception("Submitted task requires too much time!");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//
//        } else if(isCompleted(session)){
//            closeStreams();
//            this.notify();
//        }
//    }

//    private synchronized List<SimulationTask<S>> getWaitingTasks(int n){
//        List<SimulationTask<S>> fetchedTasks = new LinkedList<>();
//        for(int i = 0; i < n; i++){
//            SimulationTask<S> next = waitingTasks.poll();
//            if(next != null)
//                fetchedTasks.add(next);
//            else
//                break;
//        }
//        return fetchedTasks;
//    }

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

    private synchronized <S> List<Trajectory<S>> send(NetworkTask<S> networkTask, Socket server){
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
            state.printState();   
            if(state.isTimeout()) {
                servers.remove(server);
                //System.out.println("removed server" + server + " elapsedTime: "+state.getElapsedTime() + " Timeout: "+state.getTimeout());
            }      


        } catch (IOException | ClassNotFoundException e) {

            e.printStackTrace();
        }
        return trajectories;
    }



}