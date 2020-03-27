package quasylab.sibilla.examples.pm.seir;

import quasylab.sibilla.core.simulator.network.TCPNetworkManagerType;
import quasylab.sibilla.core.simulator.newserver.MonitoringClient;
import quasylab.sibilla.core.simulator.server.ServerInfo;
import quasylab.sibilla.core.util.NetworkUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class MonitoringClientMain {

    private static final int LOCAL_MONITORING_PORT = 59120;
    private static ServerInfo MONITORING_SERVER;

    static {
        try {
            MONITORING_SERVER = new ServerInfo(InetAddress.getByName("192.168.1.92"), 10002, TCPNetworkManagerType.DEFAULT);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new MonitoringClient(LOCAL_MONITORING_PORT, MONITORING_SERVER);
    }
}
