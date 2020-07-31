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

import quasylab.sibilla.core.network.HostLoggerSupplier;
import quasylab.sibilla.core.network.NetworkInfo;
import quasylab.sibilla.core.network.util.SSLUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * TCP based communication class that relies on TLS protocol.
 *
 * @author Stelluti Francesco Pio
 * @author Zamponi Marco
 */
public class TCPSecureNetworkManager implements TCPNetworkManager {

    private Logger LOGGER;

    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;


    /**
     * Initiates the manager as a client.
     * The socket upon which the communication is based will be built.
     *
     * @param networkInfo The network related infos about the connection that the manager will manage
     * @throws IOException
     */
    public TCPSecureNetworkManager(NetworkInfo networkInfo) throws IOException {
        this.LOGGER = HostLoggerSupplier.getInstance().getLogger();
        if (networkInfo.getType().equals(TCPNetworkManagerType.SECURE)) {
            SSLContext sslContext = SSLUtils.getInstance().createSSLContext();
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(networkInfo.getAddress(), networkInfo.getPort());
            this.buildWithSocket(sslSocket);
        } else {
            throw new IOException("Wrong TCPNetworkManager type");
        }
    }

    //The socket has already been built - Example: Server-side connection

    /**
     * Initiates the manager as a server.
     * The socket upon which the communication is based has already been built.
     *
     * @param socket upon which the network communication will be based
     * @throws IOException
     */
    public TCPSecureNetworkManager(Socket socket) throws IOException {
        this.LOGGER = HostLoggerSupplier.getInstance().getLogger();
        if (socket instanceof SSLSocket) {
            this.buildWithSocket((SSLSocket) socket);
        } else {
            throw new IOException("Wrong Socket type");
        }
    }

    /**
     * Configures and manages the TLS connection.
     *
     * @param sslSocket upon which the network communication will be based
     * @throws IOException
     */
    private void buildWithSocket(SSLSocket sslSocket) throws IOException {

        sslSocket.setEnabledCipherSuites(sslSocket.getSupportedCipherSuites());
        sslSocket.startHandshake();

        SSLSession sslSession = sslSocket.getSession();
        LOGGER.info(String.format("SSLSession Started:\n\tProtocol : %s\n\tCipher suite : %s\n\tPeer host : %s %s", sslSession.getProtocol(), sslSession.getCipherSuite(), sslSession.getPeerHost(), sslSession.getPeerPrincipal().getName()));
        this.socket = sslSocket;
        dataOutputStream = new DataOutputStream(new BufferedOutputStream(sslSocket.getOutputStream()));
        dataOutputStream.flush();
        dataInputStream = new DataInputStream(new BufferedInputStream(sslSocket.getInputStream()));
    }

    @Override
    public byte[] readObject() throws IOException {
        int length = dataInputStream.readInt();
        byte[] message = null;
        if (length > 0) {
            message = new byte[length];
            dataInputStream.readFully(message, 0, length);
        }
        return message;
    }

    @Override
    public void writeObject(byte[] toWrite) throws IOException {
        dataOutputStream.writeInt(toWrite.length);
        dataOutputStream.write(toWrite);
        dataOutputStream.flush();
    }

    @Override
    public Socket getSocket() {
        return this.socket;
    }

    @Override
    public void closeConnection() throws IOException {
        this.socket.close();
        this.dataInputStream.close();
        this.dataOutputStream.close();
    }

    @Override
    public TCPNetworkManagerType getType() {
        return TCPNetworkManagerType.SECURE;
    }
}
