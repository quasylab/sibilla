package quasylab.sibilla.core.server.ssl;

import quasylab.sibilla.core.server.ServerInfo;
import quasylab.sibilla.core.server.network.TCPNetworkManager;
import quasylab.sibilla.core.server.network.TCPNetworkManagerType;
import quasylab.sibilla.core.server.serialization.ObjectSerializer;
import quasylab.sibilla.core.util.NetworkUtils;

import java.io.IOException;

public class ClientExample {
    public static void main(String[] args) throws Exception {
        TCPNetworkManager manager = TCPNetworkManager.createNetworkManager(new ServerInfo(NetworkUtils.getLocalIp(), 10000, TCPNetworkManagerType.SECURE));
        manager.writeObject(ObjectSerializer.serializeObject("Mannaggina"));
    }
}