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

public interface TCPNetworkManager {

    static TCPNetworkManager createNetworkManager(NetworkInfo info) throws IOException {
        if (info.getType() == TCPNetworkManagerType.SECURE) {
            return new TCPSecureNetworkManager(info);
        }
        Socket socket = new Socket(info.getAddress(), info.getPort());
        return createNetworkManager((TCPNetworkManagerType) info.getType(), socket);
    }

    static TCPNetworkManager createNetworkManager(TCPNetworkManagerType networkType, Socket socket) throws IOException {
        switch (networkType) {
            case SECURE:
                return new TCPSecureNetworkManager(socket);
            case DEFAULT:
            default:
                return new TCPDefaultNetworkManager(socket);
        }
    }

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

    byte[] readObject() throws IOException;

    void writeObject(byte[] toWrite) throws IOException;

    Socket getSocket();

    default NetworkInfo getServerInfo() {
        return new NetworkInfo(getSocket().getInetAddress(), getSocket().getPort(), getType());
    }

    void closeConnection() throws IOException;

    TCPNetworkManagerType getType();
}