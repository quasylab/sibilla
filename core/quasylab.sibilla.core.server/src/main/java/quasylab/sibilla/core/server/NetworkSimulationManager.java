/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 *  Copyright (C) 2020.
 *
 *  See the NOTICE file distributed with this work for additional information
 *  regarding copyright ownership.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package quasylab.sibilla.core.server;

import org.apache.commons.math3.random.RandomGenerator;
import quasylab.sibilla.core.models.ModelDefinition;
import quasylab.sibilla.core.past.State;
import quasylab.sibilla.core.server.master.MasterCommand;
import quasylab.sibilla.core.server.master.MasterState;
import quasylab.sibilla.core.server.master.SimulationState;
import quasylab.sibilla.core.server.network.TCPNetworkManager;
import quasylab.sibilla.core.server.serialization.ClassBytesLoader;
import quasylab.sibilla.core.server.serialization.ObjectSerializer;
import quasylab.sibilla.core.server.slave.SlaveCommand;
import quasylab.sibilla.core.server.slave.SlaveState;
import quasylab.sibilla.core.simulator.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class NetworkSimulationManager<S extends State> extends QueuedSimulationManager<S> {

    private static final Logger LOGGER = Logger.getLogger(NetworkSimulationManager.class.getName());
    private final ModelDefinition<S> modelName;
    private final BlockingQueue<TCPNetworkManager> serverQueue;
    private final ExecutorService executor;
    private final SimulationState simulationState;
    private final Set<TCPNetworkManager> networkManagers;

    public NetworkSimulationManager(RandomGenerator random, Consumer<Trajectory<S>> consumer,
                                    SimulationMonitor monitor,
                                    ModelDefinition<S> modelName, SimulationState simulationState) {
        super(random, monitor,consumer);//TODO: Gestire parametro Monitor
        List<NetworkInfo> slaveNetworkInfos = simulationState.getSlaveServersStates().stream().map(SlaveState::getSlaveInfo).collect(Collectors.toList());
        LOGGER.info(String.format("Creating a new NetworkSimulationManager to contact the servers: [%s]", slaveNetworkInfos.toString()));
        this.modelName = modelName;
        this.simulationState = simulationState;
        executor = Executors.newCachedThreadPool();
        networkManagers = slaveNetworkInfos.stream().map(serverInfo -> {
            try {
                TCPNetworkManager server = TCPNetworkManager.createNetworkManager(serverInfo);
                LOGGER.info(String.format("Created a NetworkManager to contact the server - %s",
                        server.getServerInfo().toString()));
                initConnection(server);
                LOGGER.info(String.format(
                        "All the model informations have been sent to the server: %s", server.getServerInfo().toString()));
                return server;
            } catch (Exception e) {
                LOGGER.severe("Error during server initialization, removing server...");
                e.printStackTrace();
            }
            return null;
        }).collect(Collectors.toSet());
        serverQueue = new LinkedBlockingQueue<>(networkManagers);
        this.startTasksHandling();
    }

    public static SimulationManagerFactory getNetworkSimulationManagerFactory(SimulationState simulationState) {
        return new SimulationManagerFactory() {
            @Override
            public <S extends State> SimulationManager<S> getSimulationManager(RandomGenerator random, SimulationMonitor monitor, ModelDefinition<S> modelDefinition, Consumer<Trajectory<S>> consumer) {
                return new NetworkSimulationManager<>(random, consumer, monitor, modelDefinition,simulationState);
            }
        };

    }

    /**
     * Initializes a connection to the target server sending the model class
     *
     * @param server NetworkManager throught the model is passed
     * @throws Exception TODO ???
     */
    private void initConnection(TCPNetworkManager server) throws Exception {
        server.writeObject(ObjectSerializer.serializeObject(MasterCommand.INIT));
        LOGGER.info(String.format("[%s] command sent to the server - %s", MasterCommand.INIT, server.getServerInfo().toString()));
        server.writeObject(ObjectSerializer.serializeObject(modelName.getClass().getName()));
        LOGGER.info(String.format("[%s] Model name has been sent to the server - ", modelName));
        server.writeObject(ClassBytesLoader.loadClassBytes(modelName.getClass().getName()));
        LOGGER.info("Class bytes have been sent to the server - ");
    }

    @Override
    protected void startTasksHandling() {
        new Thread(this::handleTasks).start();
    }

    private void handleTasks() {
        try {
            while ((isRunning() || hasTasks() || this.simulationState.getRunningSlaveServers() > 0) && !this.simulationState.getSlaveServersStates().isEmpty()) {
                singleTaskExecution();
            }
            this.simulationState.setConcluded(true);
        } catch (InterruptedException e) {
            LOGGER.severe(e.getMessage());
        }
    }

    /**
     * Sends to the next server in the queue the task to execute and waits for the results
     *
     * @throws InterruptedException TODO ???
     */
    private void singleTaskExecution() throws InterruptedException {
        TCPNetworkManager server = findServer();
        LOGGER.info(String.format("Server currently connected to: %s",
                server.getServerInfo().toString()));
        SlaveState serverState = this.simulationState.getSlaveStateByServerInfo(server.getServerInfo());
        LOGGER.info(String.format("State of the server: [%s]", serverState.toString()));
        int acceptableTasks = serverState.getExpectedTasks();
        if (!serverState.canCompleteTask(acceptableTasks)) {
            acceptableTasks = acceptableTasks == 1 ? 1 : acceptableTasks / 2;
            LOGGER.severe(String.format("Server's tasks window has been reduced in half - %s", server.getServerInfo().toString()));
        }
        List<SimulationTask<S>> toRun = getTask(acceptableTasks, true);
        this.simulationState.setPendingTasks(this.pendingTasks());
        if (toRun.size() > 0) {
            simulationState.increaseRunningServers();
            NetworkTask<S> networkTask = new NetworkTask<>(toRun);
            CompletableFuture.supplyAsync(() -> send(networkTask, server), executor)
                    .whenComplete((value, error) -> manageResult(value, error, toRun, server));
        }
    }

    /**
     * Takes the first server from the queue
     *
     * @return First server available in the queue
     * @throws InterruptedException TODO ???
     */
    private TCPNetworkManager findServer() throws InterruptedException {
        return serverQueue.take();
    }

    /**
     * Adds a server to the queue
     *
     * @param server Server to be added to the queue
     */
    private void enqueueServer(TCPNetworkManager server) {
        serverQueue.add(server);
    }

    /**
     * Manages the results of a NetworkTask sent by a simulation server
     *
     * @param value  results of the computation
     * @param error  eventually thrown error
     * @param tasks  list of tasks executed
     * @param server server which has been used for the simulation
     */
    private void manageResult(ComputationResult<S> value, Throwable error, List<SimulationTask<S>> tasks,
                              TCPNetworkManager server) {
        LOGGER.info(String.format("Managing results by the server - %s", server.getServerInfo().toString()));
        if (error != null) {
            LOGGER.severe(String.format("Timeout occurred for server - %s",
                    server.getServerInfo().toString()));
            TCPNetworkManager newServer;
            if ((newServer = manageTimeout(server)) != null) {
                LOGGER.info(String.format("The server has responded. New server - %s", newServer.getServerInfo().toString()));
                enqueueServer(newServer);// add new server to queue, old server won't return
            } else if (this.simulationState.getSlaveServersStates().isEmpty()) {
                synchronized (this) {
                    notifyAll();
                }
            }
            rescheduleAll(tasks);
            simulationState.decreaseRunningServers();
        } else {
            LOGGER.info(String.format("Timeout did not occurred for server - %s", server.getServerInfo().toString()));
            enqueueServer(server);
            simulationState.decreaseRunningServers();
            value.getResults().forEach(this::handleTrajectory);
        }
    }

    /**
     * Manages a timeout
     *
     * @param server server whuch was in timeout
     * @return new server to use to execute tasks
     */
    private TCPNetworkManager manageTimeout(TCPNetworkManager server) {
        SlaveState oldState = this.simulationState.getSlaveStateByServerInfo(server.getServerInfo());
        TCPNetworkManager pingServer = null;
        NetworkInfo pingNetworkInfo;
        try {
            LOGGER.warning(String.format("Managing timeout of server - %s", server.getServerInfo().toString()));
            pingNetworkInfo = new NetworkInfo(server.getSocket().getInetAddress(),
                    server.getSocket().getPort(), server.getType());
            pingServer = TCPNetworkManager.createNetworkManager(pingNetworkInfo);
            pingServer.getSocket().setSoTimeout(5000); // set 5 seconds timeout on read operations
            LOGGER.info(
                    String.format("Creating a new NetworkManager to ping - %s",
                            pingServer.getServerInfo().toString()));
            oldState.timedOut(); // mark server as timed out and update GUI

            initConnection(pingServer); // initialize connection sending model data
            pingServer.writeObject(ObjectSerializer.serializeObject(MasterCommand.PING));
            LOGGER.info(String.format("Ping request sent to server - %s", pingServer.getServerInfo().toString())); // send ping request
            SlaveCommand response = (SlaveCommand) ObjectSerializer.deserializeObject(pingServer.readObject()); // wait for response
            if (!response.equals(SlaveCommand.PONG)) {
                LOGGER.severe(String.format("The response received wasn't the one expected by the server - %s", pingServer.getServerInfo().toString()));
                throw new IllegalStateException("Expected a different reply!");
            }
            LOGGER.info(
                    String.format("The response has been received within the time limit. The task window will be reduced by half for the server - %s", pingServer.getServerInfo().toString()));
            oldState.forceExpiredTimeLimit(); // halve the task window
            oldState.migrate(pingNetworkInfo);
            this.networkManagers.add(pingServer);

            server.getSocket().close();
            this.networkManagers.remove(server);
        } catch (Exception e) {
            assert pingServer != null;
            LOGGER.severe(String.format("The response has been received after the time limit. The server will be removed - %s", pingServer.getServerInfo().toString()));
            oldState.removed(); // mark server as removed and update GUI
            this.networkManagers.remove(server);
            return null;
        }
        return pingServer;
    }

    @Override
    public synchronized void join() throws InterruptedException {
        while ((getRunningTasks() > 0 || hasTasks() || this.simulationState.getRunningSlaveServers() > 0) && !this.simulationState.getSlaveServersStates().isEmpty()) {
            wait();
        }
        try {
            closeStreams();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes all the connection streams
     */
    private void closeStreams() throws Exception {
        for (TCPNetworkManager server : this.networkManagers) {
            server.writeObject(ObjectSerializer.serializeObject(MasterCommand.CLOSE_CONNECTION));
            server.writeObject(ObjectSerializer.serializeObject(this.modelName.getClass().getName()));
            server.closeConnection();
            LOGGER.info(String.format("The connection with the server has been closed - %s",
                    server.getServerInfo().toString()));
        }
    }

    /**
     * Sends tasks to execute to a server
     *
     * @param networkTask tasks to execute
     * @param server      server to send the tasks to
     * @return result of the computation
     */
    private ComputationResult<S> send(NetworkTask<S> networkTask, TCPNetworkManager server) {
        SlaveState state = this.simulationState.getSlaveStateByServerInfo(server.getServerInfo());
        ComputationResult<S> result;
        long elapsedTime;
        try {
            server.writeObject(ObjectSerializer.serializeObject(MasterCommand.TASK));
            server.writeObject(ObjectSerializer.serializeObject(networkTask));
            elapsedTime = System.nanoTime();

            server.getSocket().setSoTimeout((int) (state.getTimeout() / 1000000));
            LOGGER.info(String.format("A group of tasks has been sent to the server - %s", server.getServerInfo().toString()));

            result = (ComputationResult<S>) ObjectSerializer
                    .deserializeObject(server.readObject());

            elapsedTime = System.nanoTime() - elapsedTime;

            state.update(elapsedTime, result.getResults().size());
            LOGGER.info(String.format("The results from the computation have been received from the server - %s", server.getServerInfo().toString()));
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
            throw new RuntimeException();
        }
        return result;
    }

}