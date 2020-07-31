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

/**
 * Interface that needs to be extended from all of the communication related classes that are based upon the UDP transport protocol.
 *
 * @author Stelluti Francesco Pio
 * @author Zamponi Marco
 */
public interface UDPNetworkManager {

    /**
     * Factory method used to obtain {@link quasylab.sibilla.core.network.communication.TCPNetworkManager} implementations' instances.
     * Used in classes that want to initiate a network communication.
     *
     * @param info        The network related infos about the connection that the manager will manage
     * @param toBroadcast to mark if the manager will have to send broadcast messages
     * @return {@link quasylab.sibilla.core.network.communication.UDPNetworkManager} that will manage the requested connection
     * @throws SocketException
     */
    static UDPNetworkManager createNetworkManager(NetworkInfo info, boolean toBroadcast) throws SocketException {
        DatagramSocket socket = new DatagramSocket(info.getPort(), info.getAddress());
        socket.setBroadcast(toBroadcast);
        if (info.getType() == UDPNetworkManagerType.DEFAULT) {
            return new UDPDefaultNetworkManager(socket);
        }
        return null;
    }

    /**
     * Factory method used to obtain {@link quasylab.sibilla.core.network.communication.TCPNetworkManager} implementations' instances.
     * Used in classes that want to initiate a network communication.
     *
     * @param networkType    the type associated with the implementation of {@link quasylab.sibilla.core.network.communication.UDPNetworkManager} that will be instantiated
     * @param datagramSocket upon which the network communication will be based
     * @return {@link quasylab.sibilla.core.network.communication.UDPNetworkManager} that will manage the requested connection
     */
    static UDPNetworkManager createNetworkManager(UDPNetworkManagerType networkType, DatagramSocket datagramSocket) {
        switch (networkType) {
            case DEFAULT:
            default:
                return new UDPDefaultNetworkManager(datagramSocket);
        }
    }

    /**
     * Reads incoming data from the network.
     *
     * @return byte array of the data read from the network
     * @throws IOException
     */
    byte[] readObject() throws IOException;

    /**
     * Sends data through the network.
     *
     * @param toWrite byte array of data that will be sent over
     * @param address used as destination of the data
     * @param port    used as destination of the data
     * @throws IOException
     */
    void writeObject(byte[] toWrite, InetAddress address, int port) throws IOException;

    /**
     * Closes the network communication.
     *
     * @throws IOException
     */
    void closeConnection() throws IOException;

}
