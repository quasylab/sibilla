package quasylab.sibilla.examples.servers.slave;

import quasylab.sibilla.core.server.ServerInfo;
import quasylab.sibilla.core.server.network.TCPNetworkManagerType;
import quasylab.sibilla.core.server.slave.SlaveServerSimulationEnvironment;
import quasylab.sibilla.core.server.util.NetworkUtils;
import quasylab.sibilla.core.server.util.SSLUtils;

import java.net.UnknownHostException;
import java.util.Set;


public class SlaveApplication {
    private static final int LOCAL_DISCOVERY_PORT = 59119;
    private static final Set<ServerInfo> LOCAL_SIMULATION_SERVERS = Set.of(new ServerInfo(NetworkUtils.getLocalIp(), 8081, TCPNetworkManagerType.SECURE)
    );

    public static void main(String[] args) throws UnknownHostException {
        SSLUtils.getInstance().setKeyStoreType("JKS");
        SSLUtils.getInstance().setKeyStorePath("./slaveKeyStore.jks");
        SSLUtils.getInstance().setKeyStorePass("slavePass");
        SSLUtils.getInstance().setTrustStoreType("JKS");
        SSLUtils.getInstance().setTrustStorePath("./slaveTrustStore.jks");
        SSLUtils.getInstance().setTrustStorePass("slavePass");

        new SlaveServerSimulationEnvironment(LOCAL_DISCOVERY_PORT, LOCAL_SIMULATION_SERVERS);
    }
}
