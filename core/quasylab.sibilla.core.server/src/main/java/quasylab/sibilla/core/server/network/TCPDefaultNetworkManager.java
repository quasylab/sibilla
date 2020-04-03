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