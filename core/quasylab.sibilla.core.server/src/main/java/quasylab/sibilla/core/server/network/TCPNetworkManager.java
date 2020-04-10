package quasylab.sibilla.core.server.network;


import quasylab.sibilla.core.server.ServerInfo;
import quasylab.sibilla.core.server.util.SSLUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public interface TCPNetworkManager {

    public static TCPNetworkManager createNetworkManager(ServerInfo info) throws IOException {
        switch ((TCPNetworkManagerType) info.getType()) {
            case SECURE:
                return new TCPSecureNetworkManager(info);
            default:
                Socket socket = new Socket(info.getAddress(), info.getPort());
                return createNetworkManager((TCPNetworkManagerType) info.getType(), socket);
        }
    }

    public static TCPNetworkManager createNetworkManager(TCPNetworkManagerType networkType, Socket socket) throws IOException {
        switch (networkType) {
            case FST:
                return new TCPFSTNetworkManager(socket);
            case SECURE:
                return new TCPSecureNetworkManager(socket);
            case DEFAULT:
            default:
                return new TCPDefaultNetworkManager(socket);
        }
    }

    public static Socket createServerSocket(TCPNetworkManagerType networkType, int port) throws IOException {
        switch (networkType) {
            case SECURE:
                SSLContext sslContext = SSLUtils.getInstance().createSSLContext();
                SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();
                SSLServerSocket sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(port);
                sslServerSocket.setNeedClientAuth(true);
                return (SSLSocket) sslServerSocket.accept();

            case DEFAULT:
            default:
                ServerSocket simulationSocket = new ServerSocket(port);
                return simulationSocket.accept();

        }
    }

    public byte[] readObject() throws Exception;

    public void writeObject(byte[] toWrite) throws Exception;

    public void setTimeout(long timeout) throws SocketException;

    public Socket getSocket();

    public default ServerInfo getServerInfo() {
        return new ServerInfo(getSocket().getInetAddress(), getSocket().getPort(), getType());
    }

    public void closeConnection();

    public TCPNetworkManagerType getType();
}