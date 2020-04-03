package quasylab.sibilla.core.server.network;


import quasylab.sibilla.core.server.ServerInfo;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public interface UDPNetworkManager {

    public static UDPNetworkManager createNetworkManager(ServerInfo info, boolean toBroadcast) throws IOException {
        DatagramSocket socket = new DatagramSocket(info.getPort(), info.getAddress());
        socket.setBroadcast(toBroadcast);
        switch ((UDPNetworkManagerType) info.getType()) {
            case DEFAULT:
                return new UDPDefaultNetworkManager(socket);
        }
        return null;
    }

    public byte[] readObject() throws Exception;

    public void writeObject(byte[] toWrite, InetAddress address, int port) throws Exception;

    public void setTimeout(long timeout) throws SocketException;

    public UDPNetworkManagerType getType();

}
