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

import org.apache.commons.math3.random.RandomGenerator;
import quasylab.sibilla.core.simulator.network.TCPNetworkManager;
import quasylab.sibilla.core.simulator.newserver.Command;
import quasylab.sibilla.core.simulator.newserver.MasterState;
import quasylab.sibilla.core.simulator.newserver.SlaveState;
import quasylab.sibilla.core.simulator.pm.State;
import quasylab.sibilla.core.simulator.serialization.ClassBytesLoader;
import quasylab.sibilla.core.simulator.serialization.ObjectSerializer;
import quasylab.sibilla.core.simulator.server.ComputationResult;
import quasylab.sibilla.core.simulator.server.ServerInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class NetworkSimulationManager<S extends State> extends SimulationManager<S> {

    private static final Logger LOGGER = Logger.getLogger(NetworkSimulationManager.class.getName());
    private final String modelName;
    private BlockingQueue<TCPNetworkManager> serverQueue;
    private ExecutorService executor;
    private MasterState masterState;
    private Set<TCPNetworkManager> networkManagers;

    public NetworkSimulationManager(RandomGenerator random, Consumer<Trajectory<S>> consumer, List<ServerInfo> info,
                                    String modelName, MasterState masterState) {
        super(random, consumer);
        LOGGER.info(String.format("Creating a new NetworkSimulationManager with servers: %s \n", info.toString()));
        this.modelName = modelName;
        this.masterState = masterState;
        executor = Executors.newCachedThreadPool();
        Map<InetAddress, List<ServerInfo>> map = info.stream().collect(Collectors.toMap(ServerInfo::getAddress, s -> new ArrayList<>(Arrays.asList(s)), (l1, l2) -> {
            l1.addAll(l2);
            return l1;
        }));
        map.forEach((address, servers) -> {
            boolean classInitiated = false;
            while (!classInitiated && !servers.isEmpty()) {
                try {
                    TCPNetworkManager server = TCPNetworkManager.createNetworkManager(servers.get(0));
                    initConnection(server);
                    LOGGER.info(String.format(
                            "All the model informations have been sent to the server, throught the server: %s", servers.get(0).toString()));
                    classInitiated = true;
                } catch (Exception e) {
                    LOGGER.severe("Error during server initialization, removing server...");
                    map.get(address).remove(0);
                }
            }
        });
        map.values().stream().reduce((l1, l2) -> {
            l1.addAll(l2);
            return l1;
        }).get().forEach(serverInfo -> this.masterState.addServer(serverInfo));
        networkManagers = this.masterState.getServersMap().keySet().stream().map(serverInfo -> {
            try {
                TCPNetworkManager server = TCPNetworkManager.createNetworkManager(serverInfo);
                LOGGER.info(String.format("NetworkManager created - IP: %s - Port: %d - Class: %s",
                        server.getSocket().getInetAddress().getHostAddress(), server.getSocket().getPort(),
                        server.getClass().getName()));
                return server;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;

        }).collect(Collectors.toSet());
        serverQueue = new LinkedBlockingQueue<>(networkManagers);
        this.start();
    }

    public static SimulationManagerFactory getNetworkSimulationManagerFactory(List<ServerInfo> info,
                                                                              String modelName, MasterState masterState) {
        return new SimulationManagerFactory() {

            @Override
            public <S extends State> SimulationManager<S> getSimulationManager(RandomGenerator random,
                                                                               Consumer<Trajectory<S>> consumer) {
                return new NetworkSimulationManager<>(random, consumer, info, modelName, masterState);
            }
        };

    }

    private void initConnection(TCPNetworkManager server) throws Exception {
        server.writeObject(ObjectSerializer.serializeObject(Command.MASTER_INIT));
        LOGGER.info("Init command sent to the slave");
        server.writeObject(ObjectSerializer.serializeObject(modelName));
        LOGGER.info(String.format("Model name %s has been sent to the server", modelName));
        server.writeObject(ClassBytesLoader.loadClassBytes(modelName));
        LOGGER.info("Class bytes have been sent to the server");
    }

    @Override
    protected void start() {

        Thread t = new Thread(this::handleTasks);
        t.start();

    }

    private void handleTasks() {

        try {
            while ((isRunning() || hasTasks() || this.masterState.getRunningServers() > 0) && !this.masterState.getServersMap().isEmpty()) {
                run();
            }
        } catch (InterruptedException e) {
            LOGGER.severe(e.getMessage());
        }

    }

    private void run() throws InterruptedException {
        TCPNetworkManager server = findServer();
        LOGGER.info(String.format("Server currently connected - IP: %s - Port: %d - Class: %s",
                server.getSocket().getInetAddress().getHostAddress(), server.getSocket().getPort(),
                server.getClass().getName()));
        List<SimulationTask<S>> toRun;
        SlaveState serverState = this.masterState.getServersMap().get(server.getServerInfo());
        LOGGER.info(String.format("State of the server: %s", serverState.toString()));
        int acceptableTasks = serverState.getExpectedTasks();
        if (!serverState.canCompleteTask(acceptableTasks)) {
            acceptableTasks = acceptableTasks == 1 ? 1 : acceptableTasks / 2;
            LOGGER.severe("Server's tasks window has been reduced in half");
        }
        toRun = getTask(acceptableTasks, true);
        if (toRun.size() > 0) {
            startServerRunning();
            NetworkTask<S> networkTask = new NetworkTask<>(toRun);
            CompletableFuture.supplyAsync(() -> send(networkTask, server), executor)
                    .whenComplete((value, error) -> manageResult(value, error, toRun, server));

        }
    }

    private TCPNetworkManager findServer() throws InterruptedException {
        return serverQueue.take();
    }

    private void enqueueServer(TCPNetworkManager server) {
        serverQueue.add(server);
    }

    private synchronized void startServerRunning() {
        this.masterState.increaseRunningServers();
        LOGGER.info("Number of running server has been increased");
    }

    private synchronized void endServerRunning() {
        this.masterState.decreaseRunningServers();
        LOGGER.info("Number of running server has been decreased");
    }

    private void manageResult(ComputationResult<S> value, Throwable error, List<SimulationTask<S>> tasks,
                              TCPNetworkManager server) {
        LOGGER.info("Managing results");
        if (error != null) {
            LOGGER.severe(String.format("Timeout occurred, contacting server - IP: %s - Port: %d - Class: %s",
                    server.getSocket().getInetAddress().getHostAddress(), server.getSocket().getPort(),
                    server.getClass().getName()));
            TCPNetworkManager newServer;
            if ((newServer = manageTimeout(server)) != null) {
                LOGGER.info("The server has responded");
                enqueueServer(newServer);// add new server to queue, old server won't return
            } else if (this.masterState.getServersMap().isEmpty()) {
                synchronized (this) {
                    notifyAll();
                }
            }
            rescheduleAll(tasks);
            endServerRunning();
        } else {
            LOGGER.info("Timeout did not occurred");
            enqueueServer(server);
            endServerRunning();
            value.getResults().forEach(this::handleTrajectory);
            propertyChange("servers",
                    new String[]{
                            server.getSocket().getInetAddress().getHostAddress() + ":" + server.getSocket().getPort(),
                            this.masterState.getServersMap().get(server.getServerInfo()).toString()});
        }
    }

    private TCPNetworkManager manageTimeout(TCPNetworkManager server) {
        LOGGER.warning("Managing timeout");
        TCPNetworkManager pingServer;
        SlaveState oldState = this.masterState.getServersMap().get(server.getServerInfo()); // get old state
        ServerInfo pingServerInfo;
        try {
            oldState.timedOut(); // mark server as timed out and update GUI
            propertyChange("servers",
                    new String[]{
                            server.getSocket().getInetAddress().getHostAddress() + ":" + server.getSocket().getPort(),
                            oldState.toString()});
            pingServerInfo = new ServerInfo(server.getSocket().getInetAddress(),
                    server.getSocket().getPort(), server.getType());
            pingServer = TCPNetworkManager.createNetworkManager(pingServerInfo);
            LOGGER.info(
                    String.format("Creating a new NetworkManager to ping the server  - IP: %s - Port: %d - Class: %s",
                            server.getSocket().getInetAddress().getHostAddress(), server.getSocket().getPort(),
                            server.getClass().getName()));
            pingServer.getSocket().setSoTimeout(5000); // set 5 seconds timeout on read operations
            initConnection(pingServer); // initialize connection sending model data
            pingServer.writeObject(ObjectSerializer.serializeObject(Command.MASTER_PING));
            LOGGER.info("Ping request sent"); // send ping request
            Command response = (Command) ObjectSerializer.deserializeObject(pingServer.readObject()); // wait for response
            if (!response.equals(Command.SLAVE_PONG)) {
                LOGGER.severe("The response received wasn't the one expected");
                throw new IllegalStateException("Expected a different reply!");
            }
            LOGGER.info(
                    "The response has been received within the time limit. The task window will be reduced by half");
            oldState.forceExpiredTimeLimit(); // halve the task window
            oldState.migrate();
            this.masterState.addServer(pingServerInfo, oldState); // update hash map
            this.networkManagers.add(pingServer);
            server.getSocket().close();
            this.networkManagers.remove(server);
            //this.masterState.removeServer(server);
        } catch (Exception e) {
            LOGGER.severe("The response has been received after the time limit. The server will be removed");
            oldState.removed(); // mark server as removed and update GUI
            this.networkManagers.remove(server);
            // this.masterState.removeServer(pingServerInfo);
            propertyChange("servers",
                    new String[]{
                            server.getSocket().getInetAddress().getHostAddress() + ":" + server.getSocket().getPort(),
                            oldState.toString()});
            return null;
        }
        return pingServer;
    }

    @Override
    public synchronized void join() throws InterruptedException {
        while ((getRunningTasks() > 0 || hasTasks() || this.masterState.getRunningServers() > 0) && !this.masterState.getServersMap().isEmpty()) {
            wait();
        }
        closeStreams();
        propertyChange("end", null);
        /// time testing stuff
        // executor.shutdown();
    }

    private void closeStreams() {
        for (TCPNetworkManager server : this.networkManagers) {
            try {
                server.getSocket().close();
                LOGGER.info(String.format("The connection with the server has been closed - IP: %s - Port: %d",
                        server.getSocket().getInetAddress().getHostName(),
                        server.getSocket().getPort()));
            } catch (IOException e) {
                LOGGER.severe(e.getMessage());
            }
        }
    }

    private ComputationResult<S> send(NetworkTask<S> networkTask, TCPNetworkManager server) {
        ComputationResult<S> result;
        long elapsedTime;
        SlaveState state = this.masterState.getServersMap().get(server.getServerInfo());

        try {
            server.writeObject(ObjectSerializer.serializeObject(Command.MASTER_TASK));
            server.writeObject(ObjectSerializer.serializeObject(networkTask));
            elapsedTime = System.nanoTime();

            server.getSocket().setSoTimeout((int) (state.getTimeout() / 1000000));
            LOGGER.info("A group of tasks has been sent");
            @SuppressWarnings("unchecked")
            ComputationResult<S> receivedResult = (ComputationResult<S>) ObjectSerializer
                    .deserializeObject(server.readObject());

            elapsedTime = System.nanoTime() - elapsedTime;

            state.update(elapsedTime, receivedResult.getResults().size());
            LOGGER.info("The results from the computation have been received");
            result = receivedResult;
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
            throw new RuntimeException();
        }
        return result;
    }

}