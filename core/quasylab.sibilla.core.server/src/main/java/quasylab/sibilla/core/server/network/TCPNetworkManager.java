package quasylab.sibilla.core.server.network;


import quasylab.sibilla.core.server.ServerInfo;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public interface TCPNetworkManager {

    public static TCPNetworkManager createNetworkManager(ServerInfo info) throws IOException {
        Socket socket;
        switch ((TCPNetworkManagerType) info.getType()) {
            case SECURE:
                SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                SSLSocket sslSocket = (SSLSocket) factory.createSocket(info.getAddress(), info.getPort());
                /*sslSocket.setEnabledCipherSuites(new String[]{"TLS_DHE_DSS_WITH_AES_256_CBC_SHA256"});
                sslSocket.setEnabledProtocols(new String[]{"TLSv1.2"});*/
                return createNetworkManager((TCPNetworkManagerType) info.getType(), sslSocket);
            default:
                socket = new Socket(info.getAddress(), info.getPort());
                return createNetworkManager((TCPNetworkManagerType) info.getType(), socket);
        }
    }

    public static TCPNetworkManager createNetworkManager(TCPNetworkManagerType serType, Socket socket) throws IOException {
        switch (serType) {
            case FST:
                return new TCPFSTNetworkManager(socket);
            case DEFAULT:
            case SECURE:
            default:
                return new TCPDefaultNetworkManager(socket);
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