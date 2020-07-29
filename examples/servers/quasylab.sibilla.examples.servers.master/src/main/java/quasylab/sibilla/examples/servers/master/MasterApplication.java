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

package quasylab.sibilla.examples.servers.master;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import quasylab.sibilla.core.network.HostLoggerSupplier;
import quasylab.sibilla.core.network.master.MasterServerSimulationEnvironment;
import quasylab.sibilla.core.network.communication.TCPNetworkManagerType;
import quasylab.sibilla.core.network.communication.UDPNetworkManagerType;
import quasylab.sibilla.core.network.serialization.SerializerType;
import quasylab.sibilla.core.network.util.NetworkUtils;
import quasylab.sibilla.core.network.util.SSLUtils;
import quasylab.sibilla.core.network.util.StartupUtils;

import java.net.SocketException;
import java.util.Map;
import java.util.logging.Logger;

@SpringBootApplication
public class MasterApplication implements CommandLineRunner {

    private static Logger LOGGER;

    @Autowired
    MonitoringServerComponent monitoringServerComponent;

    public static void main(String[] args) {
        SpringApplication.run(MasterApplication.class, args);
    }

    @Override
    public void run(String... args) throws SocketException {

        LOGGER = HostLoggerSupplier.getInstance(String.format("MasterServer")).getLogger();

        final Map<String, String> options = StartupUtils.parseOptions(args);

        final int localDiscoveryPort = Integer.parseInt(options.getOrDefault("masterDiscoveryPort", "10000"));
        final int remoteDiscoveryPort = Integer.parseInt(options.getOrDefault("slaveDiscoveryPort", "59119"));
        final int localSimulationPort = Integer.parseInt(options.getOrDefault("masterSimulationPort", "10001"));

        final UDPNetworkManagerType slaveDiscoveryNetworkManagerType = StartupUtils.UDPNetworkManagerParser(options.getOrDefault("slaveDiscoveryCommunicationType", "DEFAULT"));
        final TCPNetworkManagerType clientSimulationNetworkManagerType = StartupUtils.TCPNetworkManagerParser(options.getOrDefault("clientSimulationCommunicationType", "SECURE"));

        final String keyStoreType = options.getOrDefault("keyStoreType", "JKS");
        final String keyStorePath = options.getOrDefault("keyStorePath", "masterKeyStore.jks");
        final String keyStorePass = options.getOrDefault("keyStorePass", "masterPass");
        final String trustStoreType = options.getOrDefault("trustStoreType", "JKS");
        final String trustStorePath = options.getOrDefault("trustStorePath", "masterTrustStore.jks");
        final String trustStorePass = options.getOrDefault("trustStorePass", "masterPass");

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
                        "-masterDiscoveryPort: [%d]\n" +
                        "-slaveDiscoveryPort: [%d]\n" +
                        "-masterSimulationPort: [%d]\n" +
                        "-slaveDiscoveryCommunicationType: [%s]\n" +
                        "-clientSimulationCommunicationType: [%s]",
                keyStoreType,
                keyStorePath,
                trustStoreType,
                trustStorePath,
                localDiscoveryPort,
                remoteDiscoveryPort,
                localSimulationPort,
                slaveDiscoveryNetworkManagerType,
                clientSimulationNetworkManagerType));

        new MasterServerSimulationEnvironment(localDiscoveryPort,
                remoteDiscoveryPort, slaveDiscoveryNetworkManagerType, localSimulationPort,
                clientSimulationNetworkManagerType, SerializerType.FST, monitoringServerComponent);
    }

}
