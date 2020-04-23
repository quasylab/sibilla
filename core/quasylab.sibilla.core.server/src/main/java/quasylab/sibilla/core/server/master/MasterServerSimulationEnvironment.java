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

package quasylab.sibilla.core.server.master;

import quasylab.sibilla.core.server.NetworkSimulationManager;
import quasylab.sibilla.core.server.ServerInfo;
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
import quasylab.sibilla.core.simulator.pm.State;
import quasylab.sibilla.core.simulator.sampling.SamplingFunction;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Manages connection with clients and slave servers to execute and manage the simulations' tasks and their results over network connections.
 */
public class MasterServerSimulationEnvironment {

    private static final Logger LOGGER = Logger.getLogger(MasterServerSimulationEnvironment.class.getName());

    private final ServerInfo LOCAL_DISCOVERY_INFO;
    private final ServerInfo LOCAL_SIMULATION_INFO;

    private final UDPNetworkManager discoveryNetworkManager;
    private final ExecutorService discoveryConnectionExecutor = Executors.newCachedThreadPool();

    private final int localSimulationPort;
    private final MasterState state;

    private final int remotePort;
    private final ExecutorService connectionExecutor = Executors.newCachedThreadPool();

    /**
     * Creates and starts up a master server with the given parameters.
     *
     *
     * @param localDiscoveryPort port used by the master server to manage the incoming slave servers' registration requests.
     * @param remoteDiscoveryPort port used by the slave servers to manage the incoming master server discovery message.
     * @param discoveryNetworkManager type of UDP network communication that will be used during the slave servers' discovery by the master.
     * @param localSimulationPort port used by the master server to manage the incoming clients' simulation requests.
     * @param simulationNetworkManager type of TCP network communication that will be used between master server and clients.
     * @param listeners PropertyChangeListener objects that will be updated about the state of this master server.
     * @throws IOException
     */
    public MasterServerSimulationEnvironment(int localDiscoveryPort, int remoteDiscoveryPort,
                                             UDPNetworkManagerType discoveryNetworkManager, int localSimulationPort,
                                             TCPNetworkManagerType simulationNetworkManager, PropertyChangeListener... listeners)
            throws IOException {
        LOCAL_DISCOVERY_INFO = new ServerInfo(NetworkUtils.getLocalIp(), localDiscoveryPort, discoveryNetworkManager);
        LOCAL_SIMULATION_INFO = new ServerInfo(NetworkUtils.getLocalIp(), localSimulationPort,
                simulationNetworkManager);
        this.remotePort = remoteDiscoveryPort;
        this.state = new MasterState(LOCAL_SIMULATION_INFO);
        this.localSimulationPort = localSimulationPort;
        Arrays.stream(listeners).forEach(this.state::addPropertyChangeListener);

        this.discoveryNetworkManager = UDPNetworkManager.createNetworkManager(LOCAL_DISCOVERY_INFO, true);

        // this.simulationServers = new HashSet<>();

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

    }


    /**
     * Broadcast the discovery message to all master's network interfaces
     */
    private void broadcastToInterfaces() {
        try {
            while (true) {
                state.updateServersLife();

                NetworkInterface.networkInterfaces().filter(networkInterface -> {
                    try {
                        return !networkInterface.isLoopback() && networkInterface.isUp();
                    } catch (SocketException e) {
                        e.printStackTrace();
                    }
                    return false;
                }).forEach(networkInterface -> networkInterface.getInterfaceAddresses().stream()
                        .map(InterfaceAddress::getBroadcast).filter(Objects::nonNull)
                        .forEach(this::broadcastToSingleInterface));
                LOGGER.info(String.format("Current set of servers: %s", state.getServers()));

                Thread.sleep(20000);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the server that receives messages from slave servers about their
     * informations
     */
    private void startDiscoveryServer() {
        while (true) {
            try {
                Set<ServerInfo> slaveSimulationServers = (Set<ServerInfo>) ObjectSerializer
                        .deserializeObject(discoveryNetworkManager.readObject());
                discoveryConnectionExecutor.execute(() -> {
                    try {
                        manageServers(slaveSimulationServers);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Manages the informations received by slave servers
     *
     * @param info Set of informations received by a slave server
     */
    private void manageServers(Set<ServerInfo> info) {
        info.forEach(singleInfo -> {
            if (state.addServer(singleInfo)) {
                LOGGER.info(String.format("Added simulation server - %s", singleInfo.toString()));
            }
            // LOGGER.warning("This server was already present: " + singleInfo.toString());
        });

    }

    /**
     * Starts the server that listens for simulations to execute
     */
    private void startSimulationServer() {
        try {

            ServerSocket serverSocket = TCPNetworkManager.createServerSocket((TCPNetworkManagerType) LOCAL_SIMULATION_INFO.getType(), localSimulationPort);
            LOGGER.info(String.format("The server is now listening for clients on port: [%d]",
                    LOCAL_SIMULATION_INFO.getPort()));
            while (true) {
                Socket socket = serverSocket.accept();
                connectionExecutor.execute(() -> {
                    try {
                        manageClientMessage(socket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles a message sent by a client and executes the simulation that has been
     * sent
     *
     * @param socket Socket on which the message arrived
     * @throws IOException TODO exception handling
     */
    private void manageClientMessage(Socket socket) throws IOException {
        TCPNetworkManager simulationNetworkManager = TCPNetworkManager
                .createNetworkManager((TCPNetworkManagerType) LOCAL_SIMULATION_INFO.getType(), socket);
        AtomicBoolean clientIsActive = new AtomicBoolean(true);
        try {
            Map<ClientCommand, Runnable> map = Map.of(
                    ClientCommand.PING, () -> this.respondPingRequest(simulationNetworkManager),
                    ClientCommand.INIT, () -> this.loadModelClass(simulationNetworkManager),
                    ClientCommand.DATA, () -> this.handleSimulationDataSet(simulationNetworkManager),
                    ClientCommand.CLOSE_CONNECTION, () -> this.closeConnectionWithClient(simulationNetworkManager, clientIsActive));
            while (clientIsActive.get()) {
                ClientCommand command = (ClientCommand) ObjectSerializer
                        .deserializeObject(simulationNetworkManager.readObject());
                LOGGER.info(String.format("[%s] command received by client - %s", command,
                        simulationNetworkManager.getServerInfo().toString()));
                map.getOrDefault(command, () -> {
                }).run();
            }
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }

    }

    /**
     * Closes the connection with the given client
     *
     * @param client       client which the connection has to be closed
     * @param clientActive whether the client is active or not
     */
    private void closeConnectionWithClient(TCPNetworkManager client, AtomicBoolean clientActive) {
        try {
            String modelName = (String) ObjectSerializer.deserializeObject(client.readObject());
            LOGGER.info(String.format("[%s] Model name read to be deleted by client - %s", modelName, client.getServerInfo().toString()));
            clientActive.set(false);
            CustomClassLoader.classes.remove(modelName);
            LOGGER.info(String.format("[%s] Model deleted off the class loader", modelName));
            LOGGER.info(String.format("Client closed the connection"));
            client.closeConnection();
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * The server receives the simulation datas' from the client and submits a new
     * set of simulations
     *
     * @param client the TCPNetworkManager that represents the connection with the
     *               client
     */
    private void handleSimulationDataSet(TCPNetworkManager client) {
        try {
            SimulationDataSet<State> dataSet = (SimulationDataSet<State>) ObjectSerializer
                    .deserializeObject(client.readObject());
            this.state.setTotalSimulationTasks(dataSet.getReplica());
            LOGGER.info(
                    String.format("Simulation datas received by the client - %s", client.getServerInfo().toString()));
            client.writeObject(ObjectSerializer.serializeObject(MasterCommand.DATA_RESPONSE));
            LOGGER.info(String.format("[%s] command sent to the client - %s", MasterCommand.DATA_RESPONSE,
                    client.getServerInfo().toString()));
            client.writeObject(ObjectSerializer.serializeObject(MasterCommand.RESULTS));
            client.writeObject(ObjectSerializer.serializeObject(this.submitSimulations(dataSet)));
            this.state.setTotalSimulationTasks(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * The server submits a new set of simulations
     *
     * @param dataSet containing all the simulation oriented datas
     */
    private SamplingFunction submitSimulations(SimulationDataSet dataSet) {
        SimulationEnvironment sim = new SimulationEnvironment(NetworkSimulationManager
                .getNetworkSimulationManagerFactory(dataSet.getModelName(), this.state));
        try {
            sim.simulate(dataSet.getRandomGenerator(), dataSet.getModel(),
                    dataSet.getModelInitialState(), dataSet.getModelSamplingFunction(),
                    dataSet.getReplica(), dataSet.getDeadline(), false);
            this.state.increaseExecutedSimulations();
            return dataSet.getModelSamplingFunction();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * The server receives the class containing the model upon which the simulations
     * are built
     *
     * @param client the TCPNetworkManager that represents the connection with the
     *               client
     */
    private void loadModelClass(TCPNetworkManager client) {
        try {
            String modelName = (String) ObjectSerializer.deserializeObject(client.readObject());
            LOGGER.info(String.format("[%s] Model name read by server - IP: [%s] Port: [%d]", modelName,
                    client.getSocket().getInetAddress().getHostAddress(), client.getSocket().getPort()));
            byte[] modelBytes = client.readObject();
            CustomClassLoader.defClass(modelName, modelBytes);
            String classLoadedName = Class.forName(modelName).getName();
            LOGGER.info(String.format("[%s] Class loaded with success", classLoadedName));
            client.writeObject(ObjectSerializer.serializeObject(MasterCommand.INIT_RESPONSE));
            LOGGER.info(String.format("[%s] command sent to the client - %s", MasterCommand.INIT_RESPONSE,
                    client.getServerInfo().toString()));
        } catch (Exception e) {
            e.printStackTrace();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
