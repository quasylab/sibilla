/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 * Copyright (C) 2020.
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package quasylab.sibilla.core.network.master;

import org.apache.commons.math3.random.RandomGenerator;
import quasylab.sibilla.core.models.ModelDefinition;
import quasylab.sibilla.core.network.ComputationResult;
import quasylab.sibilla.core.network.HostLoggerSupplier;
import quasylab.sibilla.core.network.NetworkInfo;
import quasylab.sibilla.core.network.NetworkTask;
import quasylab.sibilla.core.network.communication.TCPNetworkManager;
import quasylab.sibilla.core.network.compression.Compressor;
import quasylab.sibilla.core.network.loaders.ClassBytesLoader;
import quasylab.sibilla.core.network.serialization.ComputationResultSerializer;
import quasylab.sibilla.core.network.serialization.Serializer;
import quasylab.sibilla.core.network.serialization.SerializerType;
import quasylab.sibilla.core.network.slave.SlaveCommand;
import quasylab.sibilla.core.network.slave.SlaveState;
import quasylab.sibilla.core.past.State;
import quasylab.sibilla.core.simulator.*;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Handles and coordinates a simulation between the slave servers
 *
 * @param <S> The {@link quasylab.sibilla.core.past.State} of the simulation model.
 * @author Belenchia Matteo
 * @author Stelluti Francesco Pio
 * @author Zamponi Marco
 */
public class NetworkSimulationManager<S extends State> extends QueuedSimulationManager<S> {

    /**
     * Class logger.
     */
    private final Logger LOGGER;

    /**
     * {@link quasylab.sibilla.core.models.ModelDefinition} that represent the Model used in the simulation.
     */
    private final ModelDefinition<S> modelDefinition;

    /**
     * Queue of servers used to fetch the slave servers the tasks are sent to.
     */
    private final BlockingQueue<TCPNetworkManager> serverQueue;

    /**
     * Tasks handling related thread executor.
     */
    private final ExecutorService executor;

    /**
     * State of the simulation that is being executed
     */
    private final SimulationState simulationState;

    /**
     * Set of network managers associated to the connected slave servers
     */
    private final Set<TCPNetworkManager> networkManagers;

    private Serializer serializer;

    /**
     * Creates a NetworkSimulationManager with the parameters given in input
     *
     * @param random          RandomGenerator used in the simulation
     * @param consumer
     * @param monitor         TODO
     * @param modelDefinition model definition that represent the Model used in the simulation
     * @param simulationState state of the simulation that is being executed
     */
    public NetworkSimulationManager(RandomGenerator random, Consumer<Trajectory<S>> consumer, SimulationMonitor monitor,
                                    ModelDefinition<S> modelDefinition, SimulationState simulationState, SerializerType serializerType) {
        super(random, monitor, consumer);// TODO: Gestire parametro Monitor

        this.LOGGER = HostLoggerSupplier.getInstance().getLogger();
        this.serializer = Serializer.getSerializer(serializerType);
        List<NetworkInfo> slaveNetworkInfos = simulationState.getSlaveServersStates().stream()
                .map(SlaveState::getSlaveInfo).collect(Collectors.toList());
        LOGGER.info(String.format("Creating a new NetworkSimulationManager to contact the slaves: [%s]",
                slaveNetworkInfos.toString()));
        this.modelDefinition = modelDefinition;
        this.simulationState = simulationState;
        executor = Executors.newCachedThreadPool();
        networkManagers = slaveNetworkInfos.stream().map(serverInfo -> {
            try {
                TCPNetworkManager server = TCPNetworkManager.createNetworkManager(serverInfo);
                LOGGER.info(String.format("Created a NetworkManager to contact the slave: %s",
                        server.getNetworkInfo().toString()));
                initConnection(server);
                LOGGER.info(String.format("All the model informations have been sent to the slave: %s",
                        server.getNetworkInfo().toString()));
                return server;
            } catch (IOException e) {
                LOGGER.severe(String.format("[%s] Error during server initialization, removing slave", e.getMessage()));
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toSet());
        serverQueue = new LinkedBlockingQueue<>(networkManagers);
        this.startTasksHandling();
    }

    public static SimulationManagerFactory getNetworkSimulationManagerFactory(SimulationState simulationState, SerializerType serializerType) {
        return new SimulationManagerFactory() {
            @Override
            public <S extends State> SimulationManager<S> getSimulationManager(RandomGenerator random,
                                                                               SimulationMonitor monitor, ModelDefinition<S> modelDefinition, Consumer<Trajectory<S>> consumer) {
                return new NetworkSimulationManager<>(random, consumer, monitor, modelDefinition, simulationState, serializerType);
            }
        };

    }

    /**
     * Initializes a connection to the target server sending the model class
     *
     * @param slave NetworkManager through the model is passed
     */
    private void initConnection(TCPNetworkManager slave) throws IOException {
        try {
            slave.writeObject(serializer.serialize(MasterCommand.INIT));
            LOGGER.info(String.format("[%s] command sent to the slave: %s", MasterCommand.INIT,
                    slave.getNetworkInfo().toString()));
            slave.writeObject(serializer.serialize(modelDefinition.getClass().getName()));
            LOGGER.info(String.format("[%s] Model name has been sent to the slave: %s", modelDefinition.getClass().getName(), slave.getNetworkInfo().toString()));
            slave.writeObject(ClassBytesLoader.loadClassBytes(modelDefinition.getClass().getName()));
            LOGGER.info(String.format("Class bytes have been sent to the slave: %s", slave.getNetworkInfo().toString()));

            SlaveCommand answer = (SlaveCommand) serializer.deserialize(slave.readObject());
            if (answer.equals(SlaveCommand.INIT_RESPONSE)) {
                LOGGER.info(String.format("Answer received: [%s] - Slave: %s", answer, slave.getNetworkInfo().toString()));
            } else {
                throw new ClassCastException("Wrong answer after INIT command. Expected INIT_RESPONSE");
            }

        } catch (ClassCastException e) {
            LOGGER.severe(String.format("[%s] Message cast failure during the connection initialization - Slave: %s", e.getMessage(), slave.getNetworkInfo().toString()));
            throw new IOException();
        } catch (IOException e) {
            LOGGER.severe(String.format("[%s] Network communication failure during the connection initialization  - Slave: %s", e.getMessage(), slave.getNetworkInfo().toString()));
            throw new IOException();
        }
    }

    @Override
    protected void startTasksHandling() {
        new Thread(this::handleTasks).start();
    }

    private void handleTasks() {
        while ((isRunning() || hasTasks() || this.simulationState.getRunningSlaveServers() > 0)
                && !this.simulationState.getSlaveServersStates().isEmpty()) {
            singleTaskExecution();
        }
        this.simulationState.setConcluded();
        this.closeStreams();
    }

    /**
     * Sends to the next server in the queue the task to execute and waits for the
     * results
     */
    private void singleTaskExecution() {
        try {
            TCPNetworkManager server = findServer();
            LOGGER.info(String.format("Slave currently connected to: %s", server.getNetworkInfo().toString()));
            SlaveState serverState = this.simulationState.getSlaveStateByServerInfo(server.getNetworkInfo());
            LOGGER.info(String.format("State of the slave: [%s]", serverState.toString()));
            int acceptableTasks = serverState.getExpectedTasks();
            if (!serverState.canCompleteTask(acceptableTasks)) {
                acceptableTasks = acceptableTasks == 1 ? 1 : acceptableTasks / 2;
                LOGGER.severe(String.format("Server's tasks window has been reduced in half - %s",
                        server.getNetworkInfo().toString()));
            }
            LOGGER.info(String.format("Has tasks: %s", hasTasks()));
            LOGGER.info(String.format("Is running: %s", isRunning()));
            List<SimulationTask<S>> toRun = getTask(acceptableTasks, true);
            LOGGER.info(String.format("Tasks to run: %d", toRun.size()));
            this.simulationState.setPendingTasks(this.pendingTasks());
            if (toRun.size() > 0) {
                simulationState.increaseRunningServers();
                NetworkTask<S> networkTask = new NetworkTask<>(toRun);
                CompletableFuture.supplyAsync(() -> send(networkTask, server), executor)
                        .whenComplete((value, error) -> manageResult(value, error, toRun, server));
            }
        } catch (InterruptedException e) {
            LOGGER.severe(String.format("[%s] Interrupted exception", e.getMessage()));
        }
    }

    /**
     * Takes the first server from the queue
     *
     * @return First server available in the queue
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
        LOGGER.info(String.format("Managing results by the slave: %s", server.getNetworkInfo().toString()));
        if (error != null) {
            error.printStackTrace();
            LOGGER.severe(String.format("Timeout occurred for slave: %s", server.getNetworkInfo().toString()));
            TCPNetworkManager newServer;
            if ((newServer = manageTimeout(server)) != null) {
                LOGGER.info(String.format("The slave has responded. New server: %s",
                        newServer.getNetworkInfo().toString()));
                enqueueServer(newServer);// add new server to queue, old server won't return
            } else if (this.simulationState.getSlaveServersStates().isEmpty()) {
                synchronized (this) {
                    notifyAll();
                }
            }
            rescheduleAll(tasks);
            simulationState.decreaseRunningServers();
        } else {
            LOGGER.info(String.format("Timeout did not occurred for slave: %s", server.getNetworkInfo().toString()));
            enqueueServer(server);
            simulationState.decreaseRunningServers();
            value.getResults().forEach(this::handleTrajectory);
        }
    }

    /**
     * Manages a timeout
     *
     * @param server server that was in timeout
     * @return new server to use to execute tasks
     */
    private TCPNetworkManager manageTimeout(TCPNetworkManager server) {
        SlaveState oldState = this.simulationState.getSlaveStateByServerInfo(server.getNetworkInfo());
        TCPNetworkManager pingServer = null;
        NetworkInfo pingNetworkInfo;
        try {
            LOGGER.warning(String.format("Managing timeout of slave: %s", server.getNetworkInfo().toString()));
            pingNetworkInfo = new NetworkInfo(server.getSocket().getInetAddress(), server.getSocket().getPort(),
                    server.getType());
            pingServer = TCPNetworkManager.createNetworkManager(pingNetworkInfo);
            pingServer.getSocket().setSoTimeout(5000); // set 5 seconds timeout on read operations
            LOGGER.info(
                    String.format("Creating a new NetworkManager to ping slave: %s", pingServer.getNetworkInfo().toString()));
            oldState.timedOut(); // mark server as timed out

            initConnection(pingServer); // initialize connection sending model data
            pingServer.writeObject(serializer.serialize(MasterCommand.PING));
            LOGGER.info(String.format("Ping request sent to slave: %s", pingServer.getNetworkInfo().toString())); // send
            // ping
            // request
            SlaveCommand response = (SlaveCommand) serializer.deserialize(pingServer.readObject()); // wait
            // for
            // response
            if (!response.equals(SlaveCommand.PONG)) {
                LOGGER.severe(String.format("The response received wasn't the one expected by the slave: %s",
                        pingServer.getNetworkInfo().toString()));
                throw new IllegalStateException("Expected a different reply!");
            }
            LOGGER.info(String.format(
                    "The response has been received within the time limit. The task window will be reduced by half for the slave: %s",
                    pingServer.getNetworkInfo().toString()));
            oldState.forceExpiredTimeLimit(); // halve the task window
            oldState.migrate(pingNetworkInfo);
            this.networkManagers.add(pingServer);

            server.getSocket().close();
            this.networkManagers.remove(server);
        } catch (Exception e) {
            assert pingServer != null;
            LOGGER.severe(String.format(
                    "The response has been received after the time limit. The slave will be removed: %s",
                    pingServer.getNetworkInfo().toString()));
            oldState.setRemoved(); // mark server as removed
            this.networkManagers.remove(server);
            return null;
        }
        return pingServer;
    }

    @Override
    public synchronized void join() throws InterruptedException {
        while ((getRunningTasks() > 0 || hasTasks() || this.simulationState.getRunningSlaveServers() > 0)
                && !this.simulationState.getSlaveServersStates().isEmpty()) {
            wait();
        }
        closeStreams();

    }

    /**
     * Closes all the connection streams
     */
    private void closeStreams() {
        try {
            for (TCPNetworkManager server : this.networkManagers) {
                server.writeObject(serializer.serialize(MasterCommand.CLOSE_CONNECTION));
                LOGGER.info(String.format("[%s] command sent to the slave: %s", MasterCommand.CLOSE_CONNECTION,
                        server.getNetworkInfo().toString()));
                server.writeObject(serializer.serialize(this.modelDefinition.getClass().getName()));

                SlaveCommand answer = (SlaveCommand) serializer.deserialize(server.readObject());
                if (answer.equals(SlaveCommand.CLOSE_CONNECTION)) {
                    LOGGER.info(String.format("Answer received: [%s] - Slave: %s", answer, server.getNetworkInfo().toString()));
                } else {
                    throw new ClassCastException(String.format("Wrong answer after CLOSE_CONNECTION command. Expected CLOSE_CONNECTION from slave: %s ", server.getNetworkInfo().toString()));
                }

                server.closeConnection();
                LOGGER.info(String.format("Closed the connection with the slave: %s",
                        server.getNetworkInfo().toString()));
            }
        } catch (IOException e) {
            LOGGER.severe(String.format("[%s] Network communication failure during the connection closure", e.getMessage()));
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


        SlaveState state = this.simulationState.getSlaveStateByServerInfo(server.getNetworkInfo());
        ComputationResult<S> results;
        long elapsedTime;
        try {
            server.writeObject(serializer.serialize(MasterCommand.TASK));
            server.writeObject(serializer.serialize(networkTask));
            elapsedTime = System.nanoTime();

            server.getSocket().setSoTimeout((int) (state.getTimeout() / 1000000));
            LOGGER.info(String.format("A group of tasks has been sent to the server - %s",
                    server.getNetworkInfo().toString()));

            results = (ComputationResult<S>) ComputationResultSerializer.deserialize(Compressor.
                    decompress(server.readObject()), simulationState.simulationDataSet().getModel());
            elapsedTime = System.nanoTime() - elapsedTime;


            state.update(elapsedTime, results.getResults().size());
            LOGGER.info(String.format("The results from the computation have been received from the server - %s",
                    server.getNetworkInfo().toString()));
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
            throw new RuntimeException();
        }


        return results;
    }

}