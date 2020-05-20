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

import org.apache.commons.math3.random.AbstractRandomGenerator;
import quasylab.sibilla.core.models.pm.PopulationState;
import quasylab.sibilla.core.network.NetworkInfo;
import quasylab.sibilla.core.network.client.ClientSimulationEnvironment;
import quasylab.sibilla.core.network.communication.TCPNetworkManagerType;
import quasylab.sibilla.core.network.util.NetworkUtils;
import quasylab.sibilla.core.network.util.SSLUtils;
import quasylab.sibilla.core.simulator.DefaultRandomGenerator;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class ClientApplication implements Serializable {

    public final static int SAMPLINGS = 100;
    public final static double DEADLINE = 600;
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final int REPLICA = 5000;

    private static final AbstractRandomGenerator RANDOM_GENERATOR = new DefaultRandomGenerator();
    private static NetworkInfo MASTER_SERVER_INFO;

    static {
        try {
            MASTER_SERVER_INFO = new NetworkInfo(InetAddress.getByName("192.168.1.202"), 10001,
                    TCPNetworkManagerType.SECURE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] argv) throws Exception {

        SSLUtils.getInstance().setKeyStoreType("JKS");
        SSLUtils.getInstance().setKeyStorePath("clientKeyStore.jks");
        SSLUtils.getInstance().setKeyStorePass("clientPass");
        SSLUtils.getInstance().setTrustStoreType("JKS");
        SSLUtils.getInstance().setTrustStorePath("clientTrustStore.jks");
        SSLUtils.getInstance().setTrustStorePass("clientPass");

        SEIRModelDefinition modelDefinition = new SEIRModelDefinition();


        ClientSimulationEnvironment<PopulationState> client = new ClientSimulationEnvironment<PopulationState>(
                RANDOM_GENERATOR, modelDefinition, modelDefinition.createModel(), modelDefinition.state(), SEIRModelDefinition.getCollection(SAMPLINGS, DEADLINE),
                REPLICA, DEADLINE, MASTER_SERVER_INFO);

    }

}
