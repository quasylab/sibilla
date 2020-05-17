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
import quasylab.sibilla.core.network.util.SSLUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Interface that needs to be extended from all of the communication related classes that are based upon the TCP transport protocol.
 *
 * @author Stelluti Francesco Pio
 * @author Zamponi Marco
 */
public interface TCPNetworkManager {

    /**
     * Factory method used to obtain {@link quasylab.sibilla.core.network.communication.TCPNetworkManager} implementations' instances.
     * Used in classes that want to initiate a network communication.
     *
     * @param info The network related infos about the connection that the manager will manage
     * @return {@link quasylab.sibilla.core.network.communication.TCPNetworkManager} that will manage the requested connection
     * @throws IOException
     */
    static TCPNetworkManager createNetworkManager(NetworkInfo info) throws IOException {
        if (info.getType() == TCPNetworkManagerType.SECURE) {
            return new TCPSecureNetworkManager(info);
        }
        Socket socket = new Socket(info.getAddress(), info.getPort());
        return createNetworkManager((TCPNetworkManagerType) info.getType(), socket);
    }

    /**
     * Factory method used to obtain {@link quasylab.sibilla.core.network.communication.TCPNetworkManager} implementations' instances.
     * Used in classes that want to initiate a network communication.
     *
     * @param networkType the type associated with the implementation of {@link quasylab.sibilla.core.network.communication.TCPNetworkManager} that will be instantiated
     * @param socket      upon which the network communication will be based
     * @return {@link quasylab.sibilla.core.network.communication.TCPNetworkManager} that will manage the requested connection
     * @throws IOException
     */
    static TCPNetworkManager createNetworkManager(TCPNetworkManagerType networkType, Socket socket) throws IOException {
        switch (networkType) {
            case SECURE:
                return new TCPSecureNetworkManager(socket);
            case DEFAULT:
            default:
                return new TCPDefaultNetworkManager(socket);
        }
    }

    /**
     * Factory method used to obtain ServerSocket instances.
     * Used in classes that want to accept incoming network communications.
     *
     * @param networkType the type associated with the implementation of {@link quasylab.sibilla.core.network.communication.TCPNetworkManager} that will be instantiated
     * @param port        used to listen for incoming connections
     * @return ServerSocket used to accept incoming connections
     * @throws IOException
     */
    static ServerSocket createServerSocket(TCPNetworkManagerType networkType, int port) throws IOException {
        switch (networkType) {
            case SECURE:
                SSLContext sslContext = SSLUtils.getInstance().createSSLContext();
                SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();
                SSLServerSocket sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(port);
                sslServerSocket.setNeedClientAuth(true);
                return sslServerSocket;
            case DEFAULT:
            default:
                return new ServerSocket(port);

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
     * @throws IOException
     */
    void writeObject(byte[] toWrite) throws IOException;

    /**
     * @return the Socket upon which is based the network communication.
     */
    Socket getSocket();

    /**
     * @return a copy of the {@link quasylab.sibilla.core.network.NetworkInfo} instance associated with the manager.
     */
    default NetworkInfo getNetworkInfo() {
        return new NetworkInfo(getSocket().getInetAddress(), getSocket().getPort(), getType());
    }

    /**
     * Closes the network communication.
     *
     * @throws IOException
     */
    void closeConnection() throws IOException;

    /**
     * @return the {@link quasylab.sibilla.core.network.communication.TCPNetworkManagerType} associated with the {@link quasylab.sibilla.core.network.communication.TCPNetworkManager} implementation.
     */
    TCPNetworkManagerType getType();
}