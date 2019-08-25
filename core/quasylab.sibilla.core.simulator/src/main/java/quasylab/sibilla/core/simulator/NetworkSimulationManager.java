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
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;


import org.apache.commons.math3.random.RandomGenerator;

import quasylab.sibilla.core.simulator.serialization.SerializationType;
import quasylab.sibilla.core.simulator.serialization.Serializer;
import quasylab.sibilla.core.simulator.server.ComputationResult;

public class NetworkSimulationManager<S> extends SimulationManager<S> {
	
    
    private Map<Serializer, ServerState> servers = Collections.synchronizedMap(new HashMap<>());
    private final String modelName;
    private BlockingQueue<Serializer> serverQueue ;
    private ExecutorService executor;
    private volatile int serverRunning = 0;

    
    public static final SimulationManagerFactory getNetworkSimulationManagerFactory( InetAddress[] servers, int[] ports, String modelName, SerializationType[] serialization) {
    	return new SimulationManagerFactory() {
   		
			@Override
			public <S> SimulationManager<S> getSimulationManager(RandomGenerator random, Consumer<Trajectory<S>> consumer) {
				try {
					return new NetworkSimulationManager<S>(random, consumer, servers, ports, modelName, serialization);
				} catch (IOException e) {
					e.printStackTrace();
					return null;
				}
			}
    	};
		
	}


    public NetworkSimulationManager(RandomGenerator random, Consumer<Trajectory<S>> consumer,InetAddress[] servers, int[] ports, String modelName, SerializationType[] serialization)
            throws UnknownHostException, IOException {
    	super(random,consumer);
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
        this.start();
    }


    private void initConnection(Serializer server) throws Exception{
        byte[] classBytes = ClassBytesLoader.loadClassBytes(modelName);
        server.writeObject(modelName);
        server.writeObject(classBytes);
    }


	@Override
	protected void start() {

		Thread t = new Thread( this::handleTasks );
		t.start();
		
	}
	
	private void handleTasks() {
	
		try {
			while ( (isRunning() || hasTasks() || serverRunning > 0) && !servers.isEmpty() ){
				run();
            }
            System.out.println(serverRunning);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}

    private void run() throws InterruptedException {
        Serializer server = findServer();
        List<SimulationTask<S>> toRun;        
        ServerState serverState = servers.get(server);
        int acceptableTasks = serverState.getExpectedTasks();
        if (!serverState.canCompleteTask(acceptableTasks)) {
            acceptableTasks = acceptableTasks == 1 ? 1 : acceptableTasks / 2;
        }
        toRun = getTask(acceptableTasks,true);
        if(toRun.size() > 0){
            startServerRunning();
            NetworkTask<S> networkTask = new NetworkTask<S>(toRun);
            CompletableFuture.supplyAsync(() -> send(networkTask, server), executor)
                             .whenComplete((value, error) -> manageResult(value, error, toRun, server));
        }
    }

    private Serializer findServer() throws InterruptedException {
        return serverQueue.take();
    }

    private void enqueueServer(Serializer server){
        serverQueue.add(server);
    }

    private synchronized void startServerRunning(){
        serverRunning++;
    }

    private synchronized void endServerRunning(){
        serverRunning--;
    }

    private void manageResult(ComputationResult<S> value, Throwable error, List<SimulationTask<S>> tasks, Serializer server){
        if(error!=null){
            //timeout occurred, contact server
            Serializer newServer;
            if((newServer = manageTimeout(server))!= null){
                // server responded
                enqueueServer(newServer);// add new server to queue, old server won't return  
            }else if(servers.isEmpty()){
                synchronized(this){
                    notifyAll();
                }
            }
            rescheduleAll(tasks);
        }else{
            //timeout not occurred, continue as usual
            enqueueServer(server);
            value.getResults().stream().forEach(this::handleTrajectory);
            propertyChange("servers", new String[]{server.getSocket().getInetAddress().getHostAddress()+":"+server.getSocket().getPort(), servers.get(server).toString()});
        }
        endServerRunning();
    }


    private  Serializer manageTimeout(Serializer server){
        Serializer pingServer = null;
        ServerState oldState = servers.get(server); // get old state
        try {
            oldState.timedout();  // mark server as timed out and update GUI
            propertyChange("servers", new String[]{server.getSocket().getInetAddress().getHostAddress()+":"+server.getSocket().getPort(), oldState.toString()});
            pingServer = Serializer.createSerializer(new Socket(server.getSocket().getInetAddress().getHostAddress(), server.getSocket().getPort()), SerializationType.getType(server)); // start new connection
            pingServer.getSocket().setSoTimeout(5000); // set 5 seconds timeout on read operations
            initConnection(pingServer); // initialize connection sending model data
            pingServer.writeObject("PING"); // send ping request
            String response = (String) pingServer.readObject(); //wait for response
            if(!response.equals("PONG")){
                throw new IllegalStateException("Expected a different reply!");
            }
            // response received before time limit:
            oldState.forceExpiredTimeLimit(); // halve the task window
            oldState.migrate(pingServer); // change socket in serverstate
            servers.put(pingServer, oldState); //update hash map
            servers.remove(server);
        } catch (Exception e) {
            // time limit expired, or some other error happened
            oldState.removed(); //mark server as removed and update GUI
            servers.remove(server);
            propertyChange("servers", new String[]{server.getSocket().getInetAddress().getHostAddress()+":"+server.getSocket().getPort(), oldState.toString()});
            return null;
        }
        return pingServer;
    }

    @Override
    public synchronized void join() throws InterruptedException {
		while ( (getRunningTasks()>0 || hasTasks()) && !servers.isEmpty()){
			wait();
		}
        closeStreams();
        propertyChange("end", null);
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
            server.writeObject(networkTask);

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

}