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

package quasylab.sibilla.core.server.master;

import quasylab.sibilla.core.past.State;
import quasylab.sibilla.core.server.NetworkInfo;
import quasylab.sibilla.core.server.NetworkSimulationManager;
import quasylab.sibilla.core.server.SimulationDataSet;
import quasylab.sibilla.core.server.client.ClientCommand;
import quasylab.sibilla.core.server.network.TCPNetworkManager;
import quasylab.sibilla.core.server.network.TCPNetworkManagerType;
import quasylab.sibilla.core.server.network.UDPNetworkManager;
import quasylab.sibilla.core.server.network.UDPNetworkManagerType;
import quasylab.sibilla.core.server.serialization.CustomClassLoader;
import quasylab.sibilla.core.server.serialization.ObjectSerializer;
import quasylab.sibilla.core.server.util.NetworkUtils;
import quasylab.sibilla.core.simulator.SimulationEnvironment;
import quasylab.sibilla.core.simulator.sampling.SamplingFunction;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Manages connection with clients and slave servers to execute and manage the
 * simulations' tasks and their results over network connections.
 */
public class MasterServerSimulationEnvironment implements PropertyChangeListener {

    private static final Logger LOGGER = Logger.getLogger(MasterServerSimulationEnvironment.class.getName());

    private NetworkInfo LOCAL_DISCOVERY_INFO;
    private NetworkInfo LOCAL_SIMULATION_INFO;

    private UDPNetworkManager discoveryNetworkManager;
    private final ExecutorService discoveryConnectionExecutor = Executors.newCachedThreadPool();

    private int localSimulationPort;
    private MasterState state;

    private int remotePort;
    private final ExecutorService connectionExecutor = Executors.newCachedThreadPool();

    private final int discoveryTime = 5000;

    /**
     * Creates and starts up a master server with the given parameters.
     *
     * @param localDiscoveryPort       port used by the master server to manage the
     *                                 incoming slave servers' registration
     *                                 requests.
     * @param remoteDiscoveryPort      port used by the slave servers to manage the
     *                                 incoming master server discovery message.
     * @param discoveryNetworkManager  type of UDP network communication that will
     *                                 be used during the slave servers' discovery
     *                                 by the master.
     * @param localSimulationPort      port used by the master server to manage the
     *                                 incoming clients' simulation requests.
     * @param simulationNetworkManager type of TCP network communication that will
     *                                 be used between master server and clients.
     * @param listeners                PropertyChangeListener objects that will be
     *                                 updated about the state of this master
     *                                 server.
     */
    public MasterServerSimulationEnvironment(int localDiscoveryPort, int remoteDiscoveryPort,
                                             UDPNetworkManagerType discoveryNetworkManager, int localSimulationPort,
                                             TCPNetworkManagerType simulationNetworkManager, PropertyChangeListener... listeners) {
        try {
            LOCAL_DISCOVERY_INFO = new NetworkInfo(NetworkUtils.getLocalAddress(), localDiscoveryPort, discoveryNetworkManager);
            LOCAL_SIMULATION_INFO = new NetworkInfo(NetworkUtils.getLocalAddress(), localSimulationPort,
                    simulationNetworkManager);
            this.remotePort = remoteDiscoveryPort;
            this.state = new MasterState(LOCAL_SIMULATION_INFO);
            this.localSimulationPort = localSimulationPort;
            Arrays.stream(listeners).forEach(listener -> this.state.addPropertyChangeListener("Master Listener Update", listener));

            this.discoveryNetworkManager = UDPNetworkManager.createNetworkManager(LOCAL_DISCOVERY_INFO, true);

            LOGGER.info(String.format(
                    "Starting a new Master server"
                            + "\n- Local discovery port: [%d] - Discovery communication type: [%s - %s]"
                            + "\n- Local simulation handling port: [%d] - Simulation handling communication type[%s - %s]",
                    LOCAL_DISCOVERY_INFO.getPort(), LOCAL_DISCOVERY_INFO.getType().getClass(),
                    LOCAL_DISCOVERY_INFO.getType(), LOCAL_SIMULATION_INFO.getPort(),
                    LOCAL_SIMULATION_INFO.getType().getClass(), LOCAL_SIMULATION_INFO.getType()));

            ExecutorService activitiesExecutors = Executors.newCachedThreadPool();
            activitiesExecutors.execute(this::startDiscoveryServer);
            activitiesExecutors.execute(this::startSimulationServer);
            activitiesExecutors.execute(this::broadcastToInterfaces);
        } catch (SocketException e) {
            LOGGER.severe(String.format("Network interfaces exception - %s", e.getMessage()));
        }

    }

    /**
     * Broadcast the discovery message to all master's network interfaces
     */
    private void broadcastToInterfaces() {
        try {
            while (true) {
                state.resetKeepAlive();
                NetworkUtils.getBroadcastAddresses().stream().forEach(this::broadcastToSingleInterface);
                Thread.sleep(discoveryTime);
                state.cleanKeepAlive();
                LOGGER.info(String.format("Current set of servers: %s", state.getSlaveServers()));
            }
        } catch (InterruptedException e) {
            LOGGER.severe(String.format("Interrupted exception - %s", e.getMessage()));
        } catch (SocketException e) {
            LOGGER.severe(String.format("Network interfaces exception - %s", e.getMessage()));
        }
    }

    /**
     * Sends a broadcast message to a specified network interface, associated with
     * the ip passed as an argument
     */
    private void broadcastToSingleInterface(InetAddress address) {
        try {
            discoveryNetworkManager.writeObject(ObjectSerializer.serializeObject(LOCAL_DISCOVERY_INFO), address,
                    remotePort);
            LOGGER.info(String.format("Sent the discovery broadcast packet to the port: [%d]", remotePort));
        } catch (IOException e) {
            LOGGER.severe(String.format("Network communication failure during the broadcast  - %s", e.getMessage()));
        }
    }

    /**
     * Starts the server that receives messages from slave servers about their
     * informations
     */
    private void startDiscoveryServer() {
        try {
            while (true) {
                NetworkInfo slaveSimulationServer = (NetworkInfo) ObjectSerializer
                        .deserializeObject(discoveryNetworkManager.readObject());
                discoveryConnectionExecutor.execute(() -> {
                    manageServers(slaveSimulationServer);
                });
            }
        } catch (IOException e) {
            LOGGER.severe(String.format("Network communication failure during the discovery server startup  - %s", e.getMessage()));
        }
    }

    private void manageServers(NetworkInfo info) {
        if (state.addSlaveServer(info)) {
            LOGGER.info(String.format("Added simulation server - %s", info.toString()));
        }
    }

    /**
     * Starts the server that listens for simulations to execute
     */
    private void startSimulationServer() {
        try {
            ServerSocket serverSocket = TCPNetworkManager
                    .createServerSocket((TCPNetworkManagerType) LOCAL_SIMULATION_INFO.getType(), localSimulationPort);
            LOGGER.info(String.format("The server is now listening for clients on port: [%d]",
                    LOCAL_SIMULATION_INFO.getPort()));
            while (true) {
                Socket socket = serverSocket.accept();
                connectionExecutor.execute(() -> {
                    manageClientMessage(socket);
                });
            }
        } catch (IOException e) {
            LOGGER.severe(String.format("Network communication failure during the server socket startup - %s", e.getMessage()));
        }
    }

    /**
     * Handles a message sent by a client and executes the simulation that has been
     * sent
     *
     * @param socket Socket on which the message arrived
     */
    private void manageClientMessage(Socket socket) {
        try {
            TCPNetworkManager simulationNetworkManager = TCPNetworkManager
                    .createNetworkManager((TCPNetworkManagerType) LOCAL_SIMULATION_INFO.getType(), socket);
            SimulationState simulationState = new SimulationState(this.state, LOCAL_SIMULATION_INFO,
                    simulationNetworkManager.getServerInfo(), this.state.getSlaveServersNetworkInfos(), this);

            AtomicBoolean clientIsActive = new AtomicBoolean(true);

            Map<ClientCommand, Runnable> map = Map.of(ClientCommand.PING,
                    () -> this.respondPingRequest(simulationNetworkManager), ClientCommand.INIT,
                    () -> this.loadModelClass(simulationNetworkManager, simulationState), ClientCommand.DATA,
                    () -> this.handleSimulationDataSet(simulationNetworkManager, simulationState),
                    ClientCommand.CLOSE_CONNECTION,
                    () -> this.closeConnectionWithClient(simulationNetworkManager, clientIsActive, simulationState));
            while (clientIsActive.get()) {
                ClientCommand command = (ClientCommand) ObjectSerializer
                        .deserializeObject(simulationNetworkManager.readObject());
                LOGGER.info(String.format("[%s] command received by client - %s", command,
                        simulationNetworkManager.getServerInfo().toString()));
                map.getOrDefault(command, () -> {
                }).run();
            }
        } catch (IOException e) {
            LOGGER.severe(String.format("Network communication failure during client communication - %s", e.getMessage()));
        }
    }

    /**
     * Closes the connection with the given client
     *
     * @param client       client which the connection has to be closed
     * @param clientActive whether the client is active or not
     */
    private void closeConnectionWithClient(TCPNetworkManager client, AtomicBoolean clientActive,
                                           SimulationState simulationState) {
        try {
            String modelName = (String) ObjectSerializer.deserializeObject(client.readObject());
            LOGGER.info(String.format("[%s] Model name read to be deleted by client - %s", modelName,
                    client.getServerInfo().toString()));
            clientActive.set(false);
            CustomClassLoader.classes.remove(modelName);
            LOGGER.info(String.format("[%s] Model deleted off the class loader", modelName));
            LOGGER.info(String.format("Client closed the connection"));
            client.closeConnection();
        } catch (IOException e) {
            LOGGER.severe(String.format("Network communication failure during the connection closure - %s", e.getMessage()));
        }
    }

    /**
     * The server receives the simulation datas' from the client and submits a new
     * set of simulations
     *
     * @param client the TCPNetworkManager that represents the connection with the
     *               client
     */
    private void handleSimulationDataSet(TCPNetworkManager client, SimulationState simulationState) {
        try {
            SimulationDataSet<State> dataSet = (SimulationDataSet<State>) ObjectSerializer
                    .deserializeObject(client.readObject());
            simulationState.setSimulationDataSet(dataSet);
            simulationState.setClientConnection(client);
            LOGGER.info(
                    String.format("Simulation datas received by the client - %s", client.getServerInfo().toString()));
            client.writeObject(ObjectSerializer.serializeObject(MasterCommand.DATA_RESPONSE));
            LOGGER.info(String.format("[%s] command sent to the client - %s", MasterCommand.DATA_RESPONSE,
                    client.getServerInfo().toString()));
            this.submitSimulations(dataSet, simulationState);
        } catch (IOException e) {
            LOGGER.severe(String.format("Network communication failure during the simulation dataset reception - %s", e.getMessage()));
        }
    }

    /**
     * The server submits a new set of simulations
     *
     * @param dataSet containing all the simulation oriented datas
     */
    private SamplingFunction submitSimulations(SimulationDataSet dataSet, SimulationState simulationState) {
        try {
            SimulationEnvironment sim = new SimulationEnvironment(
                    NetworkSimulationManager.getNetworkSimulationManagerFactory(simulationState));
            sim.simulate(dataSet.getRandomGenerator(), dataSet.getModel(), dataSet.getModelInitialState(),
                    dataSet.getModelSamplingFunction(), dataSet.getReplica(), dataSet.getDeadline());
            this.state.increaseExecutedSimulations();
            return dataSet.getModelSamplingFunction();
        } catch (InterruptedException e) {
            LOGGER.severe(String.format("Simulation has been interrupted before its completion - %s", e.getMessage()));
        }
        return null;
    }

    /**
     * The server receives the class containing the model upon which the simulations
     * are built
     *
     * @param client the TCPNetworkManager that represents the connection with the
     *               client
     */
    private void loadModelClass(TCPNetworkManager client, SimulationState simulationState) {
        try {
            String modelName = (String) ObjectSerializer.deserializeObject(client.readObject());
            LOGGER.info(String.format("[%s] Model name read by server - IP: [%s] Port: [%d]", modelName,
                    client.getSocket().getInetAddress().getHostAddress(), client.getSocket().getPort()));
            byte[] modelBytes = client.readObject();
            CustomClassLoader.defClass(modelName, modelBytes);
            String classLoadedName = Class.forName(modelName).getName();
            simulationState.setSimulationModelName(classLoadedName);
            LOGGER.info(String.format("[%s] Class loaded with success", classLoadedName));
            client.writeObject(ObjectSerializer.serializeObject(MasterCommand.INIT_RESPONSE));
            LOGGER.info(String.format("[%s] command sent to the client - %s", MasterCommand.INIT_RESPONSE,
                    client.getServerInfo().toString()));
        } catch (ClassNotFoundException e) {
            LOGGER.severe(String.format("The simulation model was not loaded with success - %s", e.getMessage()));
        } catch (IOException e) {
            LOGGER.severe(String.format("Network communication failure during the simulation model loading - %s", e.getMessage()));
        }
    }

    /**
     * The server responds to a ping request received by the client
     *
     * @param client the TCPNetworkManager that represents the connection with the
     *               client
     */
    private void respondPingRequest(TCPNetworkManager client) {
        try {
            client.writeObject(ObjectSerializer.serializeObject(MasterCommand.PONG));
            LOGGER.info(String.format("[%s] command sent to the client - %s", MasterCommand.PONG,
                    client.getServerInfo().toString()));
        } catch (IOException e) {
            LOGGER.severe(String.format("Network communication failure during the ping response - %s", e.getMessage()));
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getNewValue() instanceof SimulationState) {
            SimulationState state = (SimulationState) evt.getNewValue();
            if (state.isConcluded()) {
                try {
                    state.clientConnection().writeObject(ObjectSerializer.serializeObject(MasterCommand.RESULTS));
                    state.clientConnection().writeObject(ObjectSerializer.serializeObject(state.simulationDataSet().getModelSamplingFunction()));
                    LOGGER.info(String.format("Results have been sent to the client - %s",
                            state.clientConnection().getServerInfo().toString()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
