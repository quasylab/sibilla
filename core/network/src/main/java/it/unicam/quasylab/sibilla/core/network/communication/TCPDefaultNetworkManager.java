/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 *             Copyright (C) 2020.
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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package it.unicam.quasylab.sibilla.core.network.communication;

import java.io.*;
import java.net.Socket;

/**
 * Simple TCP based communication class.
 *
 * @author Belenchia Matteo
 * @author Stelluti Francesco Pio
 * @author Zamponi Marco
 */
public class TCPDefaultNetworkManager implements TCPNetworkManager {

    private final Socket socket;
    private final DataInputStream dataInputStream;
    private final DataOutputStream dataOutputStream;

    /**
     * Initiates the manager.
     * The socket upon which the communication is based has already been built.
     *
     * @param socket upon which the network communication will be based
     * @throws IOException
     */
    public TCPDefaultNetworkManager(Socket socket) throws IOException {
        this.socket = socket;
        dataOutputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        dataOutputStream.flush();
        dataInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
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
        return socket;
    }

    @Override
    public void closeConnection() throws IOException {
        this.socket.close();
        this.dataInputStream.close();
        this.dataOutputStream.close();
    }

    @Override
    public TCPNetworkManagerType getType() {
        return TCPNetworkManagerType.DEFAULT;
    }

}