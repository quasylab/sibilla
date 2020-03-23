package quasylab.sibilla.examples.pm.seir;

import quasylab.sibilla.core.simulator.network.TCPNetworkManagerType;
import quasylab.sibilla.core.simulator.newserver.SlaveServerSimulationEnvironment;
import quasylab.sibilla.core.simulator.server.ServerInfo;
import quasylab.sibilla.core.util.NetworkUtils;

import java.net.UnknownHostException;
import java.util.Set;


public class SlaveServerMain {
    private static final int LOCAL_DISCOVERY_PORT = 59119;
    private static final Set<ServerInfo> LOCAL_SIMULATION_SERVERS = Set.of(new ServerInfo(NetworkUtils.getLocalIp(), 8080, TCPNetworkManagerType.DEFAULT),
            new ServerInfo(NetworkUtils.getLocalIp(), 8081, TCPNetworkManagerType.DEFAULT), new ServerInfo(NetworkUtils.getLocalIp(), 8082, TCPNetworkManagerType.DEFAULT)
    );

    public static void main(String[] args) throws UnknownHostException {
        new SlaveServerSimulationEnvironment(LOCAL_DISCOVERY_PORT, LOCAL_SIMULATION_SERVERS);
    }
}
