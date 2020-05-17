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

package quasylab.sibilla.core.network.communication;


import quasylab.sibilla.core.network.NetworkInfo;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public interface UDPNetworkManager {

    static UDPNetworkManager createNetworkManager(NetworkInfo info, boolean toBroadcast) throws SocketException {
        DatagramSocket socket = new DatagramSocket(info.getPort(), info.getAddress());
        socket.setBroadcast(toBroadcast);
        if (info.getType() == UDPNetworkManagerType.DEFAULT) {
            return new UDPDefaultNetworkManager(socket);
        }
        return null;
    }

    byte[] readObject() throws IOException;

    void writeObject(byte[] toWrite, InetAddress address, int port) throws IOException;

}
