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

package quasylab.sibilla.core.server.slave;

import quasylab.sibilla.core.server.BasicSimulationServer;
import quasylab.sibilla.core.server.ServerInfo;
import quasylab.sibilla.core.server.SimulationServer;
import quasylab.sibilla.core.server.network.TCPNetworkManagerType;
import quasylab.sibilla.core.server.network.UDPDefaultNetworkManager;
import quasylab.sibilla.core.server.serialization.ObjectSerializer;

import java.io.IOException;
import java.net.DatagramSocket;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Instantiates the single simulation servers, sends informations about them to the quasylab.sibilla.core.server.master
 */
public class SlaveServerSimulationEnvironment {

    private static final Logger LOGGER = Logger.getLogger(SlaveServerSimulationEnvironment.class.getName());

    private final int localDiscoveryPort;
    private final HashSet<ServerInfo> simulationServersInfo;
    private final Set<SimulationServer> simulationServers;
    private final Set<ServerInfo> knownMasters;

    /**
     * Create a slave server listening on a given port and creates the simulation servers with the given info
     *
     * @param localDiscoveryPort    port the server listens to
     * @param simulationServersInfo collection of ServerInfo in which simulation servers will be instantiated
     */
    public SlaveServerSimulationEnvironment(int localDiscoveryPort, Set<ServerInfo> simulationServersInfo) {
        this.localDiscoveryPort = localDiscoveryPort;
        this.simulationServersInfo = new HashSet<>(simulationServersInfo);
        this.simulationServers = new HashSet<>();
        this.knownMasters = new HashSet<>();
        LOGGER.info(String.format("Starting a new Slave server - It will respond for discovery messages on port [%d] and will create simulation servers on ports %s", localDiscoveryPort, simulationServersInfo.stream().map(ServerInfo::getPort).collect(Collectors.toList())));

        simulationServersInfo.forEach(info -> new Thread(() -> startupSingleSimulationServer(info)).start());

        new Thread(this::startDiscoveryServer).start();
    }

    /**
     * Starts up a single simulation server with the info passed
     *
     * @param info ServerInfo in which the server will be instantiated
     */
    private void startupSingleSimulationServer(ServerInfo info) {
        SimulationServer server = new BasicSimulationServer((TCPNetworkManagerType) info.getType());
        this.simulationServers.add(server);
        LOGGER.info(String.format("A new simulation server has been created - %s", info.toString()));
        try {
            server.start(info.getPort());
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
    }

    /**
     * Starts the server that listens for the masters and sends them info about the owned simulation servers
     */
    private void startDiscoveryServer() {
        try {
            DatagramSocket discoverySocket = new DatagramSocket(localDiscoveryPort);
            LOGGER.info(String.format("Now listening for discovery messages on port: [%d]", localDiscoveryPort));
            UDPDefaultNetworkManager manager = new UDPDefaultNetworkManager(discoverySocket);

            ServerInfo masterInfo = (ServerInfo) ObjectSerializer.deserializeObject(manager.readObject());
            LOGGER.info(String.format("Discovered by the master server - %s", masterInfo.toString()));
            manageDiscoveryMessage(manager, masterInfo);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Manages a message from the master
     *
     * @param manager    UDPNetworkManager that handles the sending of messages
     * @param masterInfo ServerInfo of the master server
     * @throws Exception TODO Exception handling
     */
    private void manageDiscoveryMessage(UDPDefaultNetworkManager manager, ServerInfo masterInfo) throws Exception {
        this.knownMasters.add(masterInfo);
        manager.writeObject(ObjectSerializer.serializeObject(simulationServersInfo), masterInfo.getAddress(), masterInfo.getPort());
        LOGGER.info(String.format("Sent the discovery response to the master server - %s", masterInfo.toString()));
        LOGGER.info(String.format("Currently known masters - %s", knownMasters.toString()));
    }
}
