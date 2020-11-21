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

package quasylab.sibilla.examples.servers.client;

import it.unicam.quasylab.sibilla.core.models.pm.PopulationModel;
import it.unicam.quasylab.sibilla.core.network.HostLoggerSupplier;
import it.unicam.quasylab.sibilla.core.network.NetworkInfo;
import it.unicam.quasylab.sibilla.core.network.client.ClientSimulationEnvironment;
import it.unicam.quasylab.sibilla.core.network.communication.TCPNetworkManagerType;
import it.unicam.quasylab.sibilla.core.network.serialization.SerializerType;
import it.unicam.quasylab.sibilla.core.network.util.NetworkUtils;
import it.unicam.quasylab.sibilla.core.network.util.SSLUtils;
import it.unicam.quasylab.sibilla.core.network.util.StartupUtils;
import it.unicam.quasylab.sibilla.core.simulator.DefaultRandomGenerator;
import org.apache.commons.math3.random.AbstractRandomGenerator;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Map;
import java.util.logging.Logger;

public class ClientApplication implements Serializable {

        public final static int SAMPLINGS = 100;
        public final static double DEADLINE = 600;
        private static Logger LOGGER;
        private static final long serialVersionUID = 1L;
        private static final int REPLICA = 100000;
        private static final int submitRepetitions = 1;
        private static final AbstractRandomGenerator RANDOM_GENERATOR = new DefaultRandomGenerator();

        public static void main(String... args) throws Exception {

                LOGGER = HostLoggerSupplier.getInstance(String.format("Client")).getLogger();

                final Map<String, String> options = StartupUtils.parseOptions(args);

                final String keyStoreType = options.getOrDefault("keyStoreType", "JKS");
                final String keyStorePath = options.getOrDefault("keyStorePath", "clientKeyStore.jks");
                final String keyStorePass = options.getOrDefault("keyStorePass", "clientPass");
                final String trustStoreType = options.getOrDefault("trustStoreType", "JKS");
                final String trustStorePath = options.getOrDefault("trustStorePath", "clientTrustStore.jks");
                final String trustStorePass = options.getOrDefault("trustStorePass", "clientPass");

                SSLUtils.getInstance().setKeyStoreType(keyStoreType);
                SSLUtils.getInstance().setKeyStorePath(keyStorePath);
                SSLUtils.getInstance().setKeyStorePass(keyStorePass);
                SSLUtils.getInstance().setTrustStoreType(trustStoreType);
                SSLUtils.getInstance().setTrustStorePath(trustStorePath);
                SSLUtils.getInstance().setTrustStorePass(trustStorePass);

                final String masterAddress = options.getOrDefault("masterAddress", "");
                final int masterPort = Integer.parseInt(options.getOrDefault("masterPort", "10001"));
                final TCPNetworkManagerType masterNetworkManagerType = StartupUtils
                                .TCPNetworkManagerParser(options.getOrDefault("masterCommunicationType", "SECURE"));

                final NetworkInfo masterServerInfo = new NetworkInfo(InetAddress.getByName(masterAddress), masterPort,
                                masterNetworkManagerType);
                LOGGER.info(String.format("Local address: [%s]", NetworkUtils.getLocalAddress()));
                LOGGER.info(String.format("Starting the Master Server with the params:\n" + "-keyStoreType: [%s]\n"
                                + "-keyStorePath: [%s]\n" + "-trustStoreType: [%s]\n" + "-trustStorePath: [%s]\n"
                                + "-masterAddress: [%s]\n" + "-masterPort: [%d]\n" + "-masterCommunicationType: [%s]",
                                keyStoreType, keyStorePath, trustStoreType, trustStorePath, masterAddress, masterPort,
                                masterNetworkManagerType));

                PopulationModelDefinition def = new SEIRModelDefinition();
                // def.setParameter("N",1000);
                PopulationModel model = def.createModel();

                new ClientSimulationEnvironment(RANDOM_GENERATOR, def, model, def.state(),
                                model.getSamplingFunction(SAMPLINGS, DEADLINE / SAMPLINGS), REPLICA, DEADLINE,
                                masterServerInfo, SerializerType.FST, submitRepetitions);

        }

}
