/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 *  Copyright (C) 2020.
 *
 *  See the NOTICE file distributed with this work for additional information
 *  regarding copyright ownership.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package quasylab.sibilla.core.server.network;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class TCPDefaultNetworkManager implements TCPNetworkManager {

    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    public TCPDefaultNetworkManager(Socket socket) throws IOException {
        this.socket = socket;
        dataOutputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        dataOutputStream.flush();
        dataInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
    }

    @Override
    public byte[] readObject() throws Exception {
        int length = dataInputStream.readInt();
        byte[] message = null;
        if (length > 0) {
            message = new byte[length];
            dataInputStream.readFully(message, 0, length);
        }
        return message;
    }

    @Override
    public void writeObject(byte[] toWrite) throws Exception {
        dataOutputStream.writeInt(toWrite.length);
        dataOutputStream.write(toWrite);
        dataOutputStream.flush();
    }

    @Override
    public void setTimeout(long timeout) throws SocketException {
        socket.setSoTimeout((int) (timeout / 1000000));
    }

    @Override
    public Socket getSocket() {
        return socket;
    }

    @Override
    public void closeConnection() {
        try {
            this.socket.close();
            this.dataInputStream.close();
            this.dataOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public TCPNetworkManagerType getType() {
        return TCPNetworkManagerType.DEFAULT;
    }

}