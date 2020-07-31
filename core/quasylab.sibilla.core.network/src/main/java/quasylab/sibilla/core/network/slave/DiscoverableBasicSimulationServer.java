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

package quasylab.sibilla.core.network.slave;

import quasylab.sibilla.core.network.NetworkInfo;
import quasylab.sibilla.core.network.communication.TCPNetworkManagerType;
import quasylab.sibilla.core.network.communication.UDPNetworkManager;
import quasylab.sibilla.core.network.communication.UDPNetworkManagerType;
import quasylab.sibilla.core.network.serialization.SerializerType;
import quasylab.sibilla.core.network.util.NetworkUtils;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;

/**
 * Extensions of a simple server that executes the simulations passed by a master server.
 * It replies to discovery messages sent from master servers.
 *
 * @author Stelluti Francesco Pio
 * @author Zamponi Marco
 */
public class DiscoverableBasicSimulationServer extends BasicSimulationServer {


    /**
     * Discovery's network communication related infos.
     */
    private NetworkInfo LOCAL_DISCOVERY_INFO;
    private Set<NetworkInfo> knownMasters;

    /**
     * Manages the discovery network communication with the master servers.
     */
    private UDPNetworkManager discoveryNetworkManager;

    public DiscoverableBasicSimulationServer(int localDiscoveryPort, TCPNetworkManagerType simulationNetworkManager, UDPNetworkManagerType discoveryNetworkManager, SerializerType serializerType) {
        super(simulationNetworkManager, serializerType);
        try {
            LOCAL_DISCOVERY_INFO = new NetworkInfo(NetworkUtils.getLocalAddress(), localDiscoveryPort, discoveryNetworkManager);
            this.knownMasters = new HashSet<>();

            LOGGER.info(String.format("Creating a new DiscoverableBasicSimulationServer - It will respond for discovery messages on port [%d]", localDiscoveryPort));

            new Thread(this::startDiscoveryServer).start();
        } catch (SocketException e) {
            LOGGER.severe(String.format("[%s] Network interfaces exception", e.getMessage()));
        }
    }

    /**
     * Starts the server that listens for the masters and sends them info about the owned simulation servers
     */
    private void startDiscoveryServer() {
        try {
            DatagramSocket discoverySocket = new DatagramSocket(LOCAL_DISCOVERY_INFO.getPort());
            LOGGER.info(String.format("Now listening for discovery messages on port: [%d]", LOCAL_DISCOVERY_INFO.getPort()));
            this.discoveryNetworkManager = UDPNetworkManager.createNetworkManager((UDPNetworkManagerType) LOCAL_DISCOVERY_INFO.getType(), discoverySocket);

            while (true) {
                NetworkInfo masterInfo = (NetworkInfo) serializer.deserialize(this.discoveryNetworkManager.readObject());

                LOGGER.info(String.format("Discovered the master: %s", masterInfo.toString()));
                manageDiscoveryMessage(this.discoveryNetworkManager, masterInfo);

            }

        } catch (SocketException e) {
            LOGGER.severe(String.format("[%s] Datagram socket creation exception", e.getMessage()));
        } catch (IOException e) {
            LOGGER.severe(String.format("[%s] Network communication failure during the discovery server startup", e.getMessage()));
        }
    }

    /**
     * Manages a message from the master
     *
     * @param manager    UDPNetworkManager that handles the sending of messages
     * @param masterInfo ServerInfo of the master server
     */
    private void manageDiscoveryMessage(UDPNetworkManager manager, NetworkInfo masterInfo) {
        try {
            this.knownMasters.add(masterInfo);
            manager.writeObject(serializer.serialize(this.localServerInfo), masterInfo.getAddress(), masterInfo.getPort());
            LOGGER.info(String.format("Sent the discovery response to the master: %s", masterInfo.toString()));
            LOGGER.info(String.format("Currently known masters - %s", knownMasters.toString()));
        } catch (IOException e) {
            LOGGER.severe(String.format("[%s] Network communication failure during the discovery message management - Master: %s", e.getMessage(), masterInfo.toString()));
        }
    }
}
