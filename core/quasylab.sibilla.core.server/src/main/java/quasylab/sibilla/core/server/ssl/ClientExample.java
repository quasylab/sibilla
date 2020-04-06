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
        SSLUtils.getInstance().setKeyStoreType("JKS");
        SSLUtils.getInstance().setKeyStorePath("E:\\Programmi\\GitHub\\sibilla\\core\\quasylab.sibilla.core.server\\src\\main\\java\\quasylab\\sibilla\\core\\server\\ssl\\clientTrustStore.jks");
        SSLUtils.getInstance().setKeyStorePass("sibilla");
        TCPNetworkManager connection = TCPNetworkManager.createNetworkManager(SERVER_INFO);
        connection.writeObject(ObjectSerializer.serializeObject(ClientCommand.DATA));
        System.out.println("I've sent: " + ClientCommand.DATA);
        connection.writeObject(ObjectSerializer.serializeObject(ClientCommand.CLOSE_CONNECTION));
        System.out.println("I've sent: " + ClientCommand.CLOSE_CONNECTION);
        connection.closeConnection();
        System.out.printf("Client closed connection");

    }
}