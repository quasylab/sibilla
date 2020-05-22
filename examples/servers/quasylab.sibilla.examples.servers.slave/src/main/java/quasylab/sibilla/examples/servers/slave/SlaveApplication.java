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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SlaveApplication {
    private static final int LOCAL_DISCOVERY_PORT = 59119;
    private static final int LOCAL_SIMULATION_PORT = 8082;
    private static final TCPNetworkManagerType SIMULATION_TCP_NETWORK_MANAGER = TCPNetworkManagerType.SECURE;


    public static void main(String[] args) {
        final Map<String, String> options = parseOptions(args);

        SSLUtils.getInstance().setKeyStoreType(options.getOrDefault("keyStoreType", "JKS"));
        SSLUtils.getInstance().setKeyStorePath(options.getOrDefault("keyStorePath", "slaveKeyStore.jks"));
        SSLUtils.getInstance().setKeyStorePass(options.getOrDefault("keyStorePass", "slavePass"));
        SSLUtils.getInstance().setTrustStoreType(options.getOrDefault("trustStoreType", "JKS"));
        SSLUtils.getInstance().setTrustStorePath(options.getOrDefault("trustStorePath", "slaveTrustStore.jks"));
        SSLUtils.getInstance().setTrustStorePass(options.getOrDefault("trustStorePass", "slavePass"));

        new DiscoverableBasicSimulationServer(LOCAL_DISCOVERY_PORT, SIMULATION_TCP_NETWORK_MANAGER).start(LOCAL_SIMULATION_PORT);
    }

    private static Map<String, String> parseOptions(String[] args) {
        final Map<String, String> options = new HashMap<>();

        String optionArgument = null;
        for (final String a : args) {
            if (a.charAt(0) == '-') {
                if (a.length() < 2) {
                    System.err.println("Error at argument " + a);
                    return null;
                }
                optionArgument = a;
            } else if (optionArgument != null) {
                options.put(optionArgument.substring(1), a);
                optionArgument = null;
            }
        }
        return options;
    }
}
