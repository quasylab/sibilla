package quasylab.sibilla.core.server.slave;

import quasylab.sibilla.core.server.ServerInfo;
import quasylab.sibilla.core.server.network.TCPNetworkManagerType;
import quasylab.sibilla.core.server.util.NetworkUtils;

import java.net.UnknownHostException;
import java.util.Set;


public class SlaveApplication {
    private static final int LOCAL_DISCOVERY_PORT = 59119;
    private static final Set<ServerInfo> LOCAL_SIMULATION_SERVERS = Set.of(new ServerInfo(NetworkUtils.getLocalIp(), 8081, TCPNetworkManagerType.DEFAULT)
    );

    public static void main(String[] args) throws UnknownHostException {
        new SlaveServerSimulationEnvironment(LOCAL_DISCOVERY_PORT, LOCAL_SIMULATION_SERVERS);
    }
}
