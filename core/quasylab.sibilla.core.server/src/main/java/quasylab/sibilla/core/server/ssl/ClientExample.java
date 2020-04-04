package quasylab.sibilla.core.server.ssl;

import quasylab.sibilla.core.server.ServerInfo;
import quasylab.sibilla.core.server.client.ClientCommand;
import quasylab.sibilla.core.server.master.MasterCommand;
import quasylab.sibilla.core.server.network.TCPNetworkManager;
import quasylab.sibilla.core.server.network.TCPNetworkManagerType;
import quasylab.sibilla.core.server.serialization.ObjectSerializer;
import quasylab.sibilla.core.util.NetworkUtils;

import java.io.IOException;

public class ClientExample {
    public static void main(String[] args) throws Exception {
        System.setProperty("javax.net.ssl.trustStore", "E:\\Programmi\\GitHub\\sibilla\\core\\quasylab.sibilla.core.server\\src\\main\\java\\quasylab\\sibilla\\core\\server\\ssl\\clientTrustStore.jts");
        System.setProperty("javax.net.ssl.trustStorePassword", "sibilla");
        TCPNetworkManager manager = TCPNetworkManager.createNetworkManager(new ServerInfo(NetworkUtils.getLocalIp(), 10000, TCPNetworkManagerType.SECURE));
        manager.writeObject(ObjectSerializer.serializeObject(ClientCommand.INIT));
        manager.writeObject(ObjectSerializer.serializeObject(ClientCommand.DATA));
        MasterCommand response = (MasterCommand) (ObjectSerializer.deserializeObject(manager.readObject()));
        System.out.printf("Ho letto: %s\n", response);
        if(response.equals(MasterCommand.CLOSE_CONNECTION)){
            manager.closeConnection();
            System.out.printf("Client chiude");
        }
    }
}