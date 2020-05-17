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

import org.nustaq.net.TCPObjectSocket;
import org.nustaq.serialization.FSTConfiguration;

import java.io.IOException;
import java.net.Socket;

/**
 * Efficient TCP based communication class.
 *
 * @author Stelluti Francesco Pio
 * @author Zamponi Marco
 */
public class TCPFSTNetworkManager implements TCPNetworkManager {
    private final Socket socket;
    private final TCPObjectSocket FSTSocket;
    private final FSTConfiguration conf = FSTConfiguration.getDefaultConfiguration();

    /**
     * Initiates the manager.
     * The socket upon which the communication is based has already been built.
     *
     * @param socket upon which the network communication will be based
     * @throws IOException
     */
    public TCPFSTNetworkManager(Socket socket) throws IOException {
        this.socket = socket;
        FSTSocket = new TCPObjectSocket(socket, conf);
    }

    @Override
    public byte[] readObject() throws IOException {
        try {
            return (byte[]) FSTSocket.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void writeObject(byte[] toWrite) throws IOException {
        try {
            FSTSocket.writeObject(toWrite);
        } catch (Exception e) {
            e.printStackTrace();
        }
        FSTSocket.flush();
    }

    @Override
    public Socket getSocket() {
        return socket;
    }

    @Override
    public void closeConnection() throws IOException {
        this.socket.close();
        this.FSTSocket.close();
    }

    @Override
    public TCPNetworkManagerType getType() {
        return TCPNetworkManagerType.FST;
    }

}