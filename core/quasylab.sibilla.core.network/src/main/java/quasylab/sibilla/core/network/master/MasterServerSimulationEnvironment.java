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

import quasylab.sibilla.core.network.HostLoggerSupplier;
import quasylab.sibilla.core.network.NetworkInfo;
import quasylab.sibilla.core.network.SimulationDataSet;
import quasylab.sibilla.core.network.client.ClientCommand;
import quasylab.sibilla.core.network.communication.TCPNetworkManager;
import quasylab.sibilla.core.network.communication.TCPNetworkManagerType;
import quasylab.sibilla.core.network.communication.UDPNetworkManager;
import quasylab.sibilla.core.network.communication.UDPNetworkManagerType;
import quasylab.sibilla.core.network.loaders.CustomClassLoader;
import quasylab.sibilla.core.network.serialization.Serializer;
import quasylab.sibilla.core.network.serialization.SerializerType;
import quasylab.sibilla.core.network.util.NetworkUtils;
import quasylab.sibilla.core.past.State;
import quasylab.sibilla.core.simulator.SimulationEnvironment;

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
 *
 * @author Stelluti Francesco Pio
 * @author Zamponi Marco
 */
public class MasterServerSimulationEnvironment implements PropertyChangeListener {

    /**
     * Class logger.
     */
    private final Logger LOGGER;

    /**
     * Milliseconds between two broadcast slave server discovery messages.
     */
    private final static int DISCOVERY_TIME = 15000;

    /**
     * Slave servers' discovery related thread executor.
     */
    private final ExecutorService slaveDiscoveryConnectionExecutor = Executors.newCachedThreadPool();


    /**
     * Clients' communications related thread executor.
     */
    private final ExecutorService clientConnectionExecutor = Executors.newCachedThreadPool();

    /**
     * Discovery's network communication related infos.
     */
    private NetworkInfo localDiscoveryInfo;

    /**
     * Simulation's network communication related infos.
     */
    private NetworkInfo localSimulationInfo;

    /**
     * Manages the network communication with the slave servers to be discovered.
     */
    private UDPNetworkManager discoveryNetworkManager;

    /**
     * Used by the master server to manage the incoming clients' simulation requests.
     */
    private int localSimulationPort;

    /**
     * State of the master server.
     */
    private MasterState state;

    /**
     * Used by the slave servers to manage the incoming master server discovery message.
     */
    private int remoteDiscoveryPort;

    private Serializer serializer;

    /**
     * Creates and starts up a master server with the given parameters.
     *
     * @param localDiscoveryPort       port used by the master server to manage the
     *                                 incoming slave servers' registration
     *                                 requests.
     * @param remoteDiscoveryPort      port used by the slave servers to manage the
     *                                 incoming master server discovery message.
     * @param discoveryNetworkManager  {@link quasylab.sibilla.core.network.communication.UDPNetworkManagerType} of UDP network communication that will
     *                                 be used during the slave servers' discovery
     *                                 by the master.
     * @param localSimulationPort      port used by the master server to manage the
     *                                 incoming clients' simulation requests.
     * @param simulationNetworkManager {@link quasylab.sibilla.core.network.communication.TCPNetworkManagerType} of TCP network communication that will
     *                                 be used between master server and clients.
     * @param listeners                {@link java.beans.PropertyChangeListener} instances that will be
     *                                 updated about the state of this master
     *                                 server.
     */

    public MasterServerSimulationEnvironment(int localDiscoveryPort, int remoteDiscoveryPort,
                                             UDPNetworkManagerType discoveryNetworkManager, int localSimulationPort,
                                             TCPNetworkManagerType simulationNetworkManager, SerializerType serializerType, PropertyChangeListener... listeners) {

        this.LOGGER = HostLoggerSupplier.getInstance().getLogger();

        try {
            this.serializer = Serializer.getSerializer(serializerType);
            localDiscoveryInfo = new NetworkInfo(NetworkUtils.getLocalAddress(), localDiscoveryPort, discoveryNetworkManager);
            localSimulationInfo = new NetworkInfo(NetworkUtils.getLocalAddress(), localSimulationPort,
                    simulationNetworkManager);
            this.remoteDiscoveryPort = remoteDiscoveryPort;
            this.state = new MasterState(localSimulationInfo);
            this.localSimulationPort = localSimulationPort;
            Arrays.stream(listeners).forEach(listener -> this.state.addPropertyChangeListener("Master Listener Update", listener));

            this.discoveryNetworkManager = UDPNetworkManager.createNetworkManager(localDiscoveryInfo, true);

            LOGGER.info(String.format(
                    "Starting a new Master server"
                            + "\n- Local discovery port: [%d] - Discovery communication type: [%s - %s]"
                            + "\n- Local simulation handling port: [%d] - Simulation handling communication type[%s - %s]",
                    localDiscoveryInfo.getPort(), localDiscoveryInfo.getType().getClass(),
                    localDiscoveryInfo.getType(), localSimulationInfo.getPort(),
                    localSimulationInfo.getType().getClass(), localSimulationInfo.getType()));

            ExecutorService activitiesExecutors = Executors.newCachedThreadPool();
            activitiesExecutors.execute(this::startDiscoveryServer);
            activitiesExecutors.execute(this::startSimulationServer);
            activitiesExecutors.execute(this::broadcastToInterfaces);
        } catch (SocketException e) {
            LOGGER.severe(String.format("[%s] Network interfaces exception", e.getMessage()));
        }

    }

    /**
     * Broadcasts the slave server discovery message through every master's network interface.
     */
    private void broadcastToInterfaces() {
        try {
            while (true) {
                NetworkUtils.getBroadcastAddresses().forEach(this::broadcastToSingleInterface);
                Thread.sleep(DISCOVERY_TIME);
                LOGGER.info(String.format("Current set of servers: %s", state.getSlaveServersNetworkInfos()));
            }
        } catch (InterruptedException e) {
            LOGGER.severe(String.format("[%s] Interrupted exception", e.getMessage()));
        } catch (SocketException e) {
            LOGGER.severe(String.format("[%s] Network interfaces exception", e.getMessage()));
        }
    }

    /**
     * Sends a broadcast message through a specified network interface.
     *
     * @param address of broadcast related to a specific interface.
     */
    private void broadcastToSingleInterface(InetAddress address) {
        try {
            discoveryNetworkManager.writeObject(serializer.serialize(localDiscoveryInfo), address,
                    remoteDiscoveryPort);
            LOGGER.info(String.format("Sent the discovery broadcast packet to the port: [%d]", remoteDiscoveryPort));
        } catch (IOException e) {
            LOGGER.severe(String.format("[%s] Network communication failure during the broadcast", e.getMessage()));
        }
    }

    /**
     * Starts the server that manages incoming discovery response messages from slave servers.
     */
    private void startDiscoveryServer() {
        try {
            while (true) {
                NetworkInfo slaveSimulationServer = (NetworkInfo) serializer
                        .deserialize(discoveryNetworkManager.readObject());
                slaveDiscoveryConnectionExecutor.execute(() -> manageServers(slaveSimulationServer));
            }
        } catch (ClassCastException e) {
            LOGGER.severe(String.format("[%s] Message cast failure during the discovery server startup", e.getMessage()));
        } catch (IOException e) {
            LOGGER.severe(String.format("[%s] Network communication failure during the discovery server startup", e.getMessage()));
        }
    }

    /**
     * Adds the network related informations received by a discovered slave server.
     *
     * @param info {@link quasylab.sibilla.core.network.NetworkInfo} received by a discovered slave server that will be used to submit it simulations.
     */
    private void manageServers(NetworkInfo info) {
        if (state.addSlaveServer(info)) {
            LOGGER.info(String.format("Added slave server - %s", info.toString()));
        }
    }

    /**
     * Starts the server that listens for clients that want to submit simulations.
     */
    private void startSimulationServer() {
        try {
            ServerSocket serverSocket = TCPNetworkManager
                    .createServerSocket((TCPNetworkManagerType) localSimulationInfo.getType(), localSimulationPort);
            LOGGER.info(String.format("The server is now listening for clients on port: [%d]",
                    localSimulationInfo.getPort()));
            while (true) {
                Socket socket = serverSocket.accept();
                clientConnectionExecutor.execute(() -> manageClientMessage(socket));
            }
        } catch (IOException e) {
            LOGGER.severe(String.format("[%s] Network communication failure during the server socket startup", e.getMessage()));
        }
    }

    /**
     * Handles a message received by a client.
     *
     * @param socket {@link java.net.Socket} through which the message arrived.
     */
    private void manageClientMessage(Socket socket) {
        try {
            TCPNetworkManager simulationNetworkManager = TCPNetworkManager
                    .createNetworkManager((TCPNetworkManagerType) localSimulationInfo.getType(), socket);
            SimulationState simulationState = new SimulationState(this.state, localSimulationInfo,
                    simulationNetworkManager.getNetworkInfo(), this.state.getSlaveServersNetworkInfos(), this);

            AtomicBoolean clientIsActive = new AtomicBoolean(true);

            Map<ClientCommand, Runnable> map = Map.of(ClientCommand.PING,
                    () -> this.respondPingRequest(simulationNetworkManager), ClientCommand.INIT,
                    () -> this.loadModelClass(simulationNetworkManager, simulationState), ClientCommand.DATA,
                    () -> this.handleSimulationDataSet(simulationNetworkManager, simulationState),
                    ClientCommand.CLOSE_CONNECTION,
                    () -> this.closeConnectionWithClient(simulationNetworkManager, clientIsActive));
            while (clientIsActive.get()) {
                ClientCommand command = (ClientCommand) serializer
                        .deserialize(simulationNetworkManager.readObject());
                LOGGER.info(String.format("[%s] command received by client - %s", command,
                        simulationNetworkManager.getNetworkInfo().toString()));
                map.getOrDefault(command, () -> {
                    throw new ClassCastException("Command received from client wasn't expected.");
                }).run();
            }
        } catch (ClassCastException e) {
            LOGGER.severe(String.format("[%s] Message cast failure during client communication", e.getMessage()));
        } catch (IOException e) {
            LOGGER.severe(String.format("[%s] Network communication failure during client communication", e.getMessage()));
        }
    }

    /**
     * Closes a client related network communication, removes the received simulation model from the master memory and signals that the client is no longer communicating with the master server.
     *
     * @param client       client related {@link quasylab.sibilla.core.network.communication.TCPNetworkManager} which connection has to be closed.
     * @param clientActive whether the client is active or not.
     */
    private void closeConnectionWithClient(TCPNetworkManager client, AtomicBoolean clientActive) {
        try {
            String modelName = (String) serializer.deserialize(client.readObject());
            LOGGER.info(String.format("[%s] Model name to be deleted read by client: %s", modelName,
                    client.getNetworkInfo().toString()));
            clientActive.set(false);
            CustomClassLoader.removeClassBytes(modelName);
            LOGGER.info(String.format("[%s] Model deleted off the class loader", modelName));
            client.writeObject(serializer.serialize(MasterCommand.CLOSE_CONNECTION));
            LOGGER.info(String.format("[%s] command sent to the client: %s", MasterCommand.CLOSE_CONNECTION,
                    client.getNetworkInfo().toString()));

            client.closeConnection();
            LOGGER.info(String.format("Master closed the connection with client: %s", client.getNetworkInfo().toString()));
        } catch (ClassCastException e) {
            LOGGER.severe(String.format("[%s] Message cast failure during connection closure - Client: %s", e.getMessage(), client.getNetworkInfo().toString()));
        } catch (IOException e) {
            LOGGER.severe(String.format("[%s] Network communication failure during the connection closure - Client: %s", e.getMessage(), client.getNetworkInfo().toString()));
        }
    }

    /**
     * Handles the simulation datas' from the client and submits the slave servers a new set of simulations.
     *
     * @param client          client related {@link quasylab.sibilla.core.network.communication.TCPNetworkManager}.
     * @param simulationState the state of the simulation related to the datas that need to be managed.
     */
    private void handleSimulationDataSet(TCPNetworkManager client, SimulationState simulationState) {
        try {
            SimulationDataSet<State> dataSet = (SimulationDataSet<State>) serializer
                    .deserialize(client.readObject());
            simulationState.setSimulationDataSet(dataSet);
            simulationState.setClientConnection(client);
            LOGGER.info(
                    String.format("Simulation data received by the client: %s", client.getNetworkInfo().toString()));
            client.writeObject(serializer.serialize(MasterCommand.DATA_RESPONSE));
            LOGGER.info(String.format("[%s] command sent to the client: %s", MasterCommand.DATA_RESPONSE,
                    client.getNetworkInfo().toString()));
            this.submitSimulations(client, dataSet, simulationState);
        } catch (IOException e) {
            LOGGER.severe(String.format("[%s] Network communication failure during the simulation dataset reception - Client: %s", e.getMessage(), client.getNetworkInfo().toString()));
        }
    }

    /**
     * Submits the slave servers a new set of simulations.
     *
     * @param dataSet containing all the simulation oriented datas.
     */
    private void submitSimulations(TCPNetworkManager client, SimulationDataSet dataSet, SimulationState simulationState) {
        try {
            SimulationEnvironment sim = new SimulationEnvironment(
                    NetworkSimulationManager.getNetworkSimulationManagerFactory(simulationState, serializer.getType()));
            sim.simulate(dataSet.getRandomGenerator(), dataSet.getModel(), dataSet.getModelInitialState(),
                    dataSet.getModelSamplingFunction(), dataSet.getReplica(), dataSet.getDeadline());
            this.state.increaseExecutedSimulations();
        } catch (InterruptedException e) {
            LOGGER.severe(String.format("[%s] Simulation has been interrupted before its completion - Client: %s", e.getMessage(), client.getNetworkInfo().toString()));
        }
    }

    /**
     * Manages the reception of the simulation model from the client.
     *
     * @param client          client related {@link quasylab.sibilla.core.network.communication.TCPNetworkManager}.
     * @param simulationState the state of the simulation related to the simulation model that need to be managed.
     */
    private void loadModelClass(TCPNetworkManager client, SimulationState simulationState) {
        try {
            String modelName = (String) serializer.deserialize(client.readObject());
            LOGGER.info(String.format("[%s] Model name read by client: %s", modelName,
                    client.getNetworkInfo().toString()));
            byte[] modelBytes = client.readObject();
            CustomClassLoader.defClass(modelName, modelBytes);
            String classLoadedName = Class.forName(modelName).getName();
            simulationState.setSimulationModelName(classLoadedName);
            LOGGER.info(String.format("[%s] Class loaded with success", classLoadedName));
            client.writeObject(serializer.serialize(MasterCommand.INIT_RESPONSE));
            LOGGER.info(String.format("[%s] command sent to the client: %s", MasterCommand.INIT_RESPONSE,
                    client.getNetworkInfo().toString()));
        } catch (ClassCastException e) {
            LOGGER.severe(String.format("[%s] Message cast failure during the simulation model loading - Client: %s", e.getMessage(), client.getNetworkInfo().toString()));

        } catch (ClassNotFoundException e) {
            LOGGER.severe(String.format("[%s] The simulation model was not loaded with success - Client: %s", e.getMessage(), client.getNetworkInfo().toString()));

        } catch (IOException e) {
            LOGGER.severe(String.format("[%s] Network communication failure during the simulation model loading - Client: %s", e.getMessage(), client.getNetworkInfo().toString()));

        }
    }

    /**
     * Manages a ping request from the client.
     *
     * @param client client related {@link quasylab.sibilla.core.network.communication.TCPNetworkManager}.
     */
    private void respondPingRequest(TCPNetworkManager client) {
        try {
            client.writeObject(serializer.serialize(MasterCommand.PONG));
            LOGGER.info(String.format("[%s] command sent to the client: %s", MasterCommand.PONG,
                    client.getNetworkInfo().toString()));
        } catch (IOException e) {
            LOGGER.severe(String.format("[%s] Network communication failure during the ping response - %s", e.getMessage(), client.getNetworkInfo().toString()));

        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getNewValue() instanceof SimulationState) {
            SimulationState state = (SimulationState) evt.getNewValue();
            if (state.isConcluded()) {
                try {
                    state.clientConnection().writeObject(serializer.serialize(MasterCommand.RESULTS));
                    LOGGER.info(String.format("[%s] command sent to the client: %s", MasterCommand.RESULTS,
                            state.clientConnection().getNetworkInfo().toString()));

                    state.clientConnection().writeObject(serializer.serialize(state.simulationDataSet().
                            getModelSamplingFunction()));

                    LOGGER.info(String.format("Results have been sent to the client: %s",
                            state.clientConnection().getNetworkInfo().toString()));
                } catch (IOException e) {
                    LOGGER.severe(String.format("[%s] Network communication failure during the results submit - Client: %s", e.getMessage(), state.clientConnection().getNetworkInfo().toString()));
                }
            }
        }
    }
}
