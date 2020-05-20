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

import quasylab.sibilla.core.network.slave.DiscoverableBasicSimulationServer;
import quasylab.sibilla.core.network.communication.TCPNetworkManagerType;
import quasylab.sibilla.core.network.util.SSLUtils;

import java.io.IOException;


public class SlaveApplication {
    private static final int LOCAL_DISCOVERY_PORT = 59119;
    private static final int LOCAL_SIMULATION_PORT = 8082;
    private static final TCPNetworkManagerType SIMULATION_TCP_NETWORK_MANAGER = TCPNetworkManagerType.SECURE;


    public static void main(String[] args) throws IOException {
        SSLUtils.getInstance().setKeyStoreType("JKS");
        SSLUtils.getInstance().setKeyStorePath("slaveKeyStore.jks");
        SSLUtils.getInstance().setKeyStorePass("slavePass");
        SSLUtils.getInstance().setTrustStoreType("JKS");
        SSLUtils.getInstance().setTrustStorePath("slaveTrustStore.jks");
        SSLUtils.getInstance().setTrustStorePass("slavePass");

        new DiscoverableBasicSimulationServer(LOCAL_DISCOVERY_PORT, SIMULATION_TCP_NETWORK_MANAGER).start(LOCAL_SIMULATION_PORT);

    }
}
