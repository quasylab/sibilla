package quasylab.sibilla.examples.servers.slave;

import quasylab.sibilla.core.server.DiscoverableBasicSimulationServer;
import quasylab.sibilla.core.server.network.TCPNetworkManagerType;
import quasylab.sibilla.core.server.util.SSLUtils;

import java.io.IOException;


public class SlaveApplication {
    private static final int LOCAL_DISCOVERY_PORT = 59119;
    private static final int LOCAL_SIMULATION_PORT = 8082;
    private static final TCPNetworkManagerType SIMULATION_TCP_NETWORK_MANAGER = TCPNetworkManagerType.SECURE;


    public static void main(String[] args) throws IOException {
        SSLUtils.getInstance().setKeyStoreType("JKS");
        SSLUtils.getInstance().setKeyStorePath("slaveKeyStore.jks");
        SSLUtils.getInstance().setKeyStorePass("slavePass");
        SSLUtils.getInstance().setTrustStoreType("JKS");
        SSLUtils.getInstance().setTrustStorePath("slaveTrustStore.jks");
        SSLUtils.getInstance().setTrustStorePass("slavePass");

        new DiscoverableBasicSimulationServer(LOCAL_DISCOVERY_PORT, SIMULATION_TCP_NETWORK_MANAGER).start(LOCAL_SIMULATION_PORT);

    }
}
