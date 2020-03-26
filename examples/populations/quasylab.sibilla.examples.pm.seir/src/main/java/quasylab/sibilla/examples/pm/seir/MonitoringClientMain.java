package quasylab.sibilla.examples.pm.seir;

import quasylab.sibilla.core.simulator.network.TCPNetworkManagerType;
import quasylab.sibilla.core.simulator.newserver.MonitoringClient;
import quasylab.sibilla.core.simulator.server.ServerInfo;
import quasylab.sibilla.core.util.NetworkUtils;

public class MonitoringClientMain {

    private static final int LOCAL_MONITORING_PORT = 59120;
    private static final ServerInfo MONITORING_SERVER = new ServerInfo(NetworkUtils.getLocalIp(), 10002, TCPNetworkManagerType.DEFAULT);

    public static void main(String[] args) {
        new MonitoringClient(LOCAL_MONITORING_PORT, MONITORING_SERVER);
    }
}
