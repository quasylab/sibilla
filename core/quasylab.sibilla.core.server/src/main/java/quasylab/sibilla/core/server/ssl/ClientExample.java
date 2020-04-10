package quasylab.sibilla.core.server.ssl;

import quasylab.sibilla.core.server.ServerInfo;
import quasylab.sibilla.core.server.client.ClientCommand;
import quasylab.sibilla.core.server.network.TCPNetworkManager;
import quasylab.sibilla.core.server.network.TCPNetworkManagerType;
import quasylab.sibilla.core.server.serialization.ObjectSerializer;
import quasylab.sibilla.core.server.util.NetworkUtils;
import quasylab.sibilla.core.server.util.SSLUtils;


public class ClientExample {

    private final static ServerInfo SERVER_INFO = new ServerInfo(NetworkUtils.getLocalIp(), 10000, TCPNetworkManagerType.SECURE);

    public static void main(String[] args) throws Exception {
        //System.setProperty("javax.net.debug", "all");
        SSLUtils.getInstance().setKeyStoreType("JKS");
        SSLUtils.getInstance().setKeyStorePath("./clientKeyStore.jks");
        SSLUtils.getInstance().setKeyStorePass("clientpass");
        SSLUtils.getInstance().setTrustStoreType("JKS");
        SSLUtils.getInstance().setTrustStorePath("./clientTrustStore.jks");
        SSLUtils.getInstance().setTrustStorePass("clientpass");
        TCPNetworkManager connection = TCPNetworkManager.createNetworkManager(SERVER_INFO);
        connection.writeObject(ObjectSerializer.serializeObject(ClientCommand.DATA));
        System.out.println("I've sent: " + ClientCommand.DATA);
        connection.writeObject(ObjectSerializer.serializeObject(ClientCommand.CLOSE_CONNECTION));
        System.out.println("I've sent: " + ClientCommand.CLOSE_CONNECTION);
        connection.closeConnection();
        System.out.printf("Client closed connection");

    }
}