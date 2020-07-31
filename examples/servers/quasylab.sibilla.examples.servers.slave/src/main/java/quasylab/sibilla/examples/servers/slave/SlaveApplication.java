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

package quasylab.sibilla.examples.servers.slave;

import quasylab.sibilla.core.network.HostLoggerSupplier;
import quasylab.sibilla.core.network.communication.UDPNetworkManagerType;
import quasylab.sibilla.core.network.serialization.SerializerType;
import quasylab.sibilla.core.network.slave.DiscoverableBasicSimulationServer;
import quasylab.sibilla.core.network.communication.TCPNetworkManagerType;
import quasylab.sibilla.core.network.util.NetworkUtils;
import quasylab.sibilla.core.network.util.SSLUtils;
import quasylab.sibilla.core.network.util.StartupUtils;

import java.net.SocketException;
import java.util.Map;
import java.util.logging.Logger;


public class SlaveApplication {

    private static Logger LOGGER;

    public static void main(String[] args) throws SocketException {

        LOGGER = HostLoggerSupplier.getInstance(String.format("SlaveServer")).getLogger();

        final Map<String, String> options = StartupUtils.parseOptions(args);

        final int localDiscoveryPort = Integer.parseInt(options.getOrDefault("slaveDiscoveryPort", "59119"));
        final int localSimulationPort = Integer.parseInt(options.getOrDefault("slaveSimulationPort", "8082"));

        final UDPNetworkManagerType masterDiscoveryNetworkManagerType = StartupUtils.UDPNetworkManagerParser(options.getOrDefault("masterDiscoveryCommunicationType", "DEFAULT"));
        final TCPNetworkManagerType masterSimulationNetworkManagerType = StartupUtils.TCPNetworkManagerParser(options.getOrDefault("masterSimulationCommunicationType", "SECURE"));

        final String keyStoreType = options.getOrDefault("keyStoreType", "JKS");
        final String keyStorePath = options.getOrDefault("keyStorePath", "slaveKeyStore.jks");
        final String keyStorePass = options.getOrDefault("keyStorePass", "slavePass");
        final String trustStoreType = options.getOrDefault("trustStoreType", "JKS");
        final String trustStorePath = options.getOrDefault("trustStorePath", "slaveTrustStore.jks");
        final String trustStorePass = options.getOrDefault("trustStorePass", "slavePass");

        SSLUtils.getInstance().setKeyStoreType(keyStoreType);
        SSLUtils.getInstance().setKeyStorePath(keyStorePath);
        SSLUtils.getInstance().setKeyStorePass(keyStorePass);
        SSLUtils.getInstance().setTrustStoreType(trustStoreType);
        SSLUtils.getInstance().setTrustStorePath(trustStorePath);
        SSLUtils.getInstance().setTrustStorePass(trustStorePass);
        LOGGER.info(String.format("Local address: [%s]", NetworkUtils.getLocalAddress()));
        LOGGER.info(String.format("Starting the Master Server with the params:\n" +
                        "-keyStoreType: [%s]\n" +
                        "-keyStorePath: [%s]\n" +
                        "-trustStoreType: [%s]\n" +
                        "-trustStorePath: [%s]\n" +
                        "-slaveDiscoveryPort: [%d]\n" +
                        "-slaveSimulationPort: [%d]\n" +
                        "-masterDiscoveryCommunicationType: [%s]\n" +
                        "-masterSimulationCommunicationType: [%s]",
                keyStoreType,
                keyStorePath,
                trustStoreType,
                trustStorePath,
                localDiscoveryPort,
                localSimulationPort,
                masterDiscoveryNetworkManagerType,
                masterSimulationNetworkManagerType));


        new DiscoverableBasicSimulationServer(localDiscoveryPort, masterSimulationNetworkManagerType, masterDiscoveryNetworkManagerType, SerializerType.FST).start(localSimulationPort);
    }


}
