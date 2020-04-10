package quasylab.sibilla.core.server.ssl;


import quasylab.sibilla.core.server.client.ClientCommand;
import quasylab.sibilla.core.server.network.TCPNetworkManager;
import quasylab.sibilla.core.server.network.TCPNetworkManagerType;
import quasylab.sibilla.core.server.serialization.ObjectSerializer;
import quasylab.sibilla.core.server.util.SSLUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class ServerExample {

    private static final int PORT = 10000;

    public static void main(String[] args) throws Exception {
        //  System.setProperty("javax.net.debug", "all");
        SSLUtils.getInstance().setKeyStoreType("JKS");
        SSLUtils.getInstance().setKeyStorePath("./serverKeyStore.jks");
        SSLUtils.getInstance().setKeyStorePass("serverpass");
        SSLUtils.getInstance().setTrustStoreType("JKS");
        SSLUtils.getInstance().setTrustStorePath("./serverTrustStore.jks");
        SSLUtils.getInstance().setTrustStorePass("serverpass");
        SSLContext sslContext = SSLUtils.getInstance().createSSLContext();
        SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();
        SSLServerSocket sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(PORT);
        sslServerSocket.setNeedClientAuth(true);
        SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();
        TCPNetworkManager clientConnection = TCPNetworkManager.createNetworkManager(TCPNetworkManagerType.SECURE, sslSocket);
        while (true) {
            ClientCommand result = (ClientCommand) ObjectSerializer.deserializeObject(clientConnection.readObject());
            System.out.printf("I've read: %s\n", result);
            if (result.equals(ClientCommand.CLOSE_CONNECTION)) {
                clientConnection.closeConnection();
                System.out.printf("Master closed connection");
                break;
            }
        }
    }
}

