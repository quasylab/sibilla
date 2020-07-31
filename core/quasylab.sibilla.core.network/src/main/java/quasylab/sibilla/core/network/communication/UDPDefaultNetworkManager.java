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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Simple communication class based upon the UDP transport protocol.
 *
 * @author Stelluti Francesco Pio
 * @author Zamponi Marco
 */
public class UDPDefaultNetworkManager implements UDPNetworkManager {

    private final DatagramSocket socket;

    /**
     * Initiates the manager as a client.
     * The socket upon which the communication is based has already been built.
     *
     * @param socket upon which the network communication will be based
     */
    public UDPDefaultNetworkManager(DatagramSocket socket) {
        this.socket = socket;
    }

    @Override
    public byte[] readObject() throws IOException {
        DatagramPacket p = new DatagramPacket(new byte[1500], 1500);
        socket.receive(p);
        return p.getData();
    }

    @Override
    public void writeObject(byte[] toWrite, InetAddress address, int port) throws IOException {
        DatagramPacket p = new DatagramPacket(toWrite, toWrite.length, address, port);
        socket.send(p);
    }

    @Override
    public void closeConnection() throws IOException {
        socket.close();
    }

}
