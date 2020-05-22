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
import quasylab.sibilla.core.network.master.MasterServerSimulationEnvironment;
import quasylab.sibilla.core.network.communication.TCPNetworkManagerType;
import quasylab.sibilla.core.network.communication.UDPNetworkManagerType;
import quasylab.sibilla.core.network.util.SSLUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class MasterApplication implements CommandLineRunner {

    private static final int LOCAL_DISCOVERY_PORT = 10000;
    private static final int REMOTE_DISCOVERY_PORT = 59119;
    private static final UDPNetworkManagerType DISCOVERY_NETWORK_MANAGER_TYPE = UDPNetworkManagerType.DEFAULT;
    private static final int LOCAL_SIMULATION_PORT = 10001;
    private static final TCPNetworkManagerType SIMULATION_NETWORK_MANAGER_TYPE = TCPNetworkManagerType.SECURE;
    @Autowired
    MonitoringServerComponent monitoringServerComponent;

    public static void main(String[] args) {
        SpringApplication.run(MasterApplication.class, args);
    }

    @Override
    public void run(String... args) {
        final Map<String, String> options = parseOptions(args);

        SSLUtils.getInstance().setKeyStoreType(options.getOrDefault("keyStoreType", "JKS"));
        SSLUtils.getInstance().setKeyStorePath(options.getOrDefault("keyStorePath", "masterKeyStore.jks"));
        SSLUtils.getInstance().setKeyStorePass(options.getOrDefault("keyStorePass", "masterPass"));
        SSLUtils.getInstance().setTrustStoreType(options.getOrDefault("trustStoreType", "JKS"));
        SSLUtils.getInstance().setTrustStorePath(options.getOrDefault("trustStorePath", "masterTrustStore.jks"));
        SSLUtils.getInstance().setTrustStorePass(options.getOrDefault("trustStorePass", "masterPass"));

        new MasterServerSimulationEnvironment(LOCAL_DISCOVERY_PORT, REMOTE_DISCOVERY_PORT, DISCOVERY_NETWORK_MANAGER_TYPE, LOCAL_SIMULATION_PORT, SIMULATION_NETWORK_MANAGER_TYPE, monitoringServerComponent);
    }

    private Map<String, String> parseOptions(String[] args) {
        final Map<String, String> options = new HashMap<>();

        String optionArgument = null;
        for (final String a : args) {
            if (a.charAt(0) == '-') {
                if (a.length() < 2) {
                    System.out.println("Invalid parameter: " + a);
                    continue;
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
