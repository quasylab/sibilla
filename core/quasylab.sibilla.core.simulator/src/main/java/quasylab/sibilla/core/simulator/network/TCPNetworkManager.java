package quasylab.sibilla.core.simulator.network;

import quasylab.sibilla.core.simulator.server.ServerInfo;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public interface TCPNetworkManager {

    public byte[] readObject() throws Exception;

    public void writeObject(byte[] toWrite) throws Exception;

    public void setTimeout(long timeout) throws SocketException;

    public Socket getSocket();

    public default ServerInfo getServerInfo(){
        return new ServerInfo(getSocket().getInetAddress(), getSocket().getPort(), getType());
    }

    public void closeConnection();
    public TCPNetworkManagerType getType();

    public static TCPNetworkManager createNetworkManager(ServerInfo info) throws IOException {
        Socket socket = new Socket(info.getAddress(), info.getPort());
        switch ((TCPNetworkManagerType) info.getType()) {
            case FST:
                return new TCPFSTNetworkManager(socket);
            case DEFAULT:
            default:
                return new TCPDefaultNetworkManager(socket);
        }
    }

    public static TCPNetworkManager createNetworkManager(TCPNetworkManagerType serType, Socket socket) throws IOException {
        switch (serType) {
            case FST:
                return new TCPFSTNetworkManager(socket);
            case DEFAULT:
            default:
                return new TCPDefaultNetworkManager(socket);
        }
    }
}